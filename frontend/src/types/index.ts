export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  isStreaming?: boolean
  toolCalls?: ToolCallInfo[]
}

export interface ToolCallInfo {
  name: string
  args: Record<string, string>
  result?: string
}

export interface ChatSession {
  id: string
  title: string
  createdAt: number
  lastMessageAt: number
}

export interface ChatRequest {
  message: string
  sessionId: string
}
