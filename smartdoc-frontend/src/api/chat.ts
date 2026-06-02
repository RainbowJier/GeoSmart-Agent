import type { ApiResponse, ChatRequest } from '@/types'

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  const json: ApiResponse<T> = await response.json()
  if (json.code !== 200) throw new Error(json.msg || `Error ${json.code}`)
  return json.data
}

export function streamChat(
  request: ChatRequest,
  onToken: (token: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): AbortController {
  const controller = new AbortController()

  fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)

      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      let eventData: string[] = []
      let inEvent = false

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            eventData.push(line.substring(5))
            inEvent = true
          } else if (inEvent && line === '') {
            const data = eventData.join('\n')
            eventData = []
            inEvent = false
            if (data === '[DONE]') {
              onComplete()
            } else {
              onToken(data)
            }
          }
        }
      }

      // Process any trailing event data
      if (inEvent) {
        const data = eventData.join('\n')
        if (data === '[DONE]') {
          onComplete()
        } else if (data) {
          onToken(data)
        }
      }
      onComplete()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        onError(err)
      }
    })

  return controller
}

export async function getChatHistory(sessionId: string): Promise<{ role: string; content: string }[]> {
  const response = await fetch(`/api/chat/history/${encodeURIComponent(sessionId)}`)
  return parseResponse<{ role: string; content: string }[]>(response)
}

export async function clearSession(sessionId: string): Promise<void> {
  const response = await fetch(`/api/chat/session/${encodeURIComponent(sessionId)}`, {
    method: 'DELETE'
  })
  if (!response.ok) throw new Error(`Failed to clear session: ${response.statusText}`)
}

