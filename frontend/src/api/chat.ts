import type { ChatRequest } from '@/types'

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
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const reader = response.body!.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            if (data.trim() === '[DONE]') {
              onComplete()
            } else {
              onToken(data)
            }
          }
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

export async function uploadDocument(file: File): Promise<{ status: string; filename: string }> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/documents/upload', {
    method: 'POST',
    body: formData
  })

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.statusText}`)
  }

  return response.json()
}

export async function listDocuments(): Promise<{ documents: string[] }> {
  const response = await fetch('/api/documents')
  return response.json()
}
