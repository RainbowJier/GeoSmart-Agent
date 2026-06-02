// ============================================================================
// types/index.ts - TypeScript 类型定义
// ============================================================================
//
// 【教学要点】TypeScript 的核心价值之一是类型安全。集中定义接口（interface）
// 可以确保组件、API、Store 之间的数据结构一致，编译期就能发现类型错误。
//
// 本文件定义了聊天功能涉及的四个核心数据结构：
//   - ChatMessage：单条聊天消息
//   - ToolCallInfo：AI 工具调用信息（当 AI 调用后端 @Tool 方法时返回）
//   - ChatSession：一个聊天会话（包含多条消息）
//   - ChatRequest：发送给后端的请求体
// ============================================================================

/** 单条聊天消息 */
export interface ChatMessage {
  id: string                              // 消息唯一标识
  role: 'user' | 'assistant'              // 发送者角色：用户 或 AI 助手
  content: string                         // 消息文本内容
  timestamp: number                       // 发送时间戳（毫秒）
  isStreaming?: boolean                   // 是否正在流式输出中
  toolCalls?: ToolCallInfo[]              // AI 调用的工具列表（可选）
}

/** AI 工具调用信息 —— 记录 AI 调用了哪个后端 @Tool 方法 */
export interface ToolCallInfo {
  name: string                            // 工具名称，如 "getPlotDetail"
  args: Record<string, string>            // 工具参数
  result?: string                         // 工具执行结果
}

/** 聊天会话 —— 一个会话包含多轮对话 */
export interface ChatSession {
  id: string                              // 会话唯一标识
  title: string                           // 会话标题（取自用户第一条消息）
  createdAt: number                       // 创建时间戳
  lastMessageAt: number                   // 最后一条消息的时间戳
}

/** 发送给后端的聊天请求体 */
export interface ChatRequest {
  message: string                         // 用户输入的消息文本
  sessionId: string                       // 当前会话 ID
}

/** 后端统一响应包装 AjaxResult<T> */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}
