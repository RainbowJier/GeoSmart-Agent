import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatMessage, ChatSession } from '@/types'
import { streamChat } from '@/api/chat'

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
      title: '新对话',
      createdAt: Date.now(),
      lastMessageAt: Date.now()
    }
    sessions.value.unshift(session)
    messages.value.set(id, [])
    currentSessionId.value = id
    return id
  }

  function selectSession(id: string) {
    currentSessionId.value = id
  }

  function sendMessage(content: string) {
    if (!currentSessionId.value) {
      createSession()
    }

    const sessionId = currentSessionId.value
    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content,
      timestamp: Date.now()
    }

    const current = messages.value.get(sessionId) || []
    messages.value.set(sessionId, [...current, userMsg])

    // Update session title from first message
    const session = sessions.value.find((s) => s.id === sessionId)
    if (session && current.length === 0) {
      session.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
    }

    // Add assistant placeholder
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
          last.content = `错误: ${error.message}`
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
    messages,
    isLoading,
    currentMessages,
    createSession,
    selectSession,
    sendMessage,
    stopStreaming
  }
})
