import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatMessage, ChatSession } from '@/types'
import { streamChat, getChatHistory, clearSession as apiClearSession } from '@/api/chat'

function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string>('')
  const messages = ref<Map<string, ChatMessage[]>>(new Map())
  const isLoading = ref(false)

  let abortController: AbortController | null = null

  const currentMessages = computed(() => {
    return messages.value.get(currentSessionId.value) || []
  })

  function createSession(): string {
    const id = generateId()
    const session: ChatSession = {
      id,
      title: 'New Chat',
      createdAt: Date.now(),
      lastMessageAt: Date.now()
    }
    sessions.value.unshift(session)
    messages.value.set(id, [])
    currentSessionId.value = id
    return id
  }

  function ensureSession(): string {
    if (!currentSessionId.value) {
      return createSession()
    }
    return currentSessionId.value
  }

  async function selectSession(id: string) {
    currentSessionId.value = id
    if (!messages.value.has(id)) {
      messages.value.set(id, [])
    }
    try {
      const history = await getChatHistory(id)
      const msgs: ChatMessage[] = history.map((h) => ({
        id: generateId(),
        role: h.role as 'user' | 'assistant',
        content: h.content,
        timestamp: Date.now()
      }))
      messages.value.set(id, msgs)
    } catch {
      // history endpoint may not exist in older backends - just show empty
    }
  }

  async function deleteSession(id: string) {
    try {
      await apiClearSession(id)
    } catch {
      // ignore if backend doesn't support it
    }
    sessions.value = sessions.value.filter((s) => s.id !== id)
    messages.value.delete(id)
    if (currentSessionId.value === id) {
      currentSessionId.value = sessions.value[0]?.id || ''
    }
  }

  function sendMessage(content: string) {
    const sessionId = ensureSession()

    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content,
      timestamp: Date.now()
    }
    const current = messages.value.get(sessionId) || []
    messages.value.set(sessionId, [...current, userMsg])

    const session = sessions.value.find((s) => s.id === sessionId)
    if (session && current.length === 0) {
      session.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
    }

    const assistantMsg: ChatMessage = {
      id: generateId(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true
    }
    messages.value.set(sessionId, [...(messages.value.get(sessionId) || []), assistantMsg])
    isLoading.value = true

    abortController = streamChat(
      { message: content, sessionId },

      (token) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content += token
          messages.value.set(sessionId, [...msgs])
        }
      },

      () => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      },

      (error) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content = `Error: ${error.message}`
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      }
    )
  }

  function stopStreaming() {
    abortController?.abort()
    isLoading.value = false
  }

  return {
    sessions,
    currentSessionId,
    isLoading,
    currentMessages,
    createSession,
    selectSession,
    deleteSession,
    sendMessage,
    stopStreaming
  }
})
