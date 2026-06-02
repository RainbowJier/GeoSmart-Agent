<template>
  <div class="msg-scroll" ref="listRef">

    <!-- Welcome -->
    <div v-if="chatStore.currentMessages.length === 0" class="welcome animate-fade-in">
      <div class="welcome-card">
        <div class="welcome-header">
          <span class="status-dot animate-pulse-glow"></span>
          <span class="status-text mono-text">SYSTEM ACTIVE // SPRING_CODE.OS</span>
        </div>
        <div class="welcome-body">
          <h1 class="welcome-title">
            <span class="welcome-icon mono-text">&gt;_</span>
            Spring Code Assistant
          </h1>
          <p class="welcome-sub">
            Your high-performance developer sandbox and cognitive companion for Java and the Spring Framework ecosystem.
          </p>
          <div class="welcome-divider"></div>
          <span class="actions-label mono-text">CHOOSE A BOOTSTRAP COMMAND:</span>
          <div class="quick-actions">
            <button class="quick-card" @click="chatStore.sendMessage('How do I create a new Spring Boot project from scratch?')">
              <span class="card-tag mono-text">// BOOTSTRAP</span>
              <span class="card-title">Spring Boot Setup</span>
              <span class="card-desc">Initialize a new maven skeleton structure.</span>
            </button>
            <button class="quick-card" @click="chatStore.sendMessage('Explain how Spring dependency injection and the IoC container work')">
              <span class="card-tag mono-text">// ARCHITECTURE</span>
              <span class="card-title">Dependency Injection</span>
              <span class="card-desc">Understand IoC control flow & bean lifecycles.</span>
            </button>
            <button class="quick-card" @click="chatStore.sendMessage('How do I build a production-ready RESTful API with Spring MVC?')">
              <span class="card-tag mono-text">// BEST PRACTICE</span>
              <span class="card-title">REST API Design</span>
              <span class="card-desc">Configure mappings, controllers, and JSON responses.</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Messages -->
    <TransitionGroup v-else name="msg-fade" tag="div" class="thread">
      <div
        v-for="msg in chatStore.currentMessages"
        :key="msg.id"
        :class="['msg-block', msg.role]"
      >
        <!-- User message -->
        <div v-if="msg.role === 'user'" class="user-msg">
          <span class="msg-prompt mono-text">&gt;</span>
          <span class="user-text mono-text">{{ msg.content }}</span>
        </div>

        <!-- AI message -->
        <div v-else class="ai-msg">
          <div
            class="markdown-body animate-fade-in"
            v-html="msg.isStreaming ? streamingHtml : getRenderedContent(msg)"
          />
          <div v-if="!msg.isStreaming" class="msg-footer">
            <button class="copy-btn" @click="copyMessage(msg.content, $event)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              Copy
            </button>
          </div>
        </div>
      </div>
    </TransitionGroup>
  </div>

  <!-- Mermaid Zoom Lightbox Modal -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="activeZoomSvg" class="zoom-modal" @click="closeZoom">
        <div class="zoom-modal-content" @click.stop>
          <header class="zoom-modal-header">
            <span class="mono-text">// DIAGRAM VIEWER</span>
            <div class="zoom-modal-actions">
              <button class="modal-btn" @click="resetZoom">
                Reset View
              </button>
              <button class="modal-btn" @click="zoomScale = Math.max(0.5, zoomScale - 0.25)">
                Zoom Out
              </button>
              <span class="scale-label mono-text">{{ Math.round(zoomScale * 100) }}%</span>
              <button class="modal-btn" @click="zoomScale = Math.min(3, zoomScale + 0.25)">
                Zoom In
              </button>
              <button class="modal-btn export-action" @click="downloadModalPng">
                Export PNG
              </button>
              <button class="modal-btn" @click="downloadModalSvg">
                Export SVG
              </button>
              <button class="modal-close-btn" @click="closeZoom">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
          </header>
          <div 
            class="zoom-viewport"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseUp"
            @wheel.prevent="handleWheel"
            style="cursor: grab; user-select: none;"
          >
            <div 
              class="zoom-wrapper"
              :style="{ transform: `translate(${panX}px, ${panY}px) scale(${zoomScale})` }"
              v-html="activeZoomSvg"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import type { ChatMessage } from '@/types'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import yaml from 'highlight.js/lib/languages/yaml'
import go from 'highlight.js/lib/languages/go'
import mermaid from 'mermaid'

mermaid.initialize({
  theme: 'neutral',
  themeVariables: {
    primaryColor: '#54b16c',
    primaryTextColor: '#18181b',
    primaryBorderColor: '#54b16c',
    lineColor: '#6b7280',
    secondaryColor: '#f0f5f1',
    tertiaryColor: '#fafafa',
    background: '#ffffff',
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
    fontSize: '14px',
  },
  flowchart: {
    layout: 'elk',
    elk: { nodePlacementStrategy: 'SIMPLE' },
    curve: 'basis',
    padding: 16,
    nodeSpacing: 50,
    rankSpacing: 80,
    htmlLabels: true,
  } as any,
  sequence: {
    diagramMarginX: 50, diagramMarginY: 30, actorMargin: 80,
    width: 200, height: 65, boxMargin: 20, boxTextMargin: 8,
    noteMargin: 20, messageMargin: 60, mirrorActors: false,
    bottomMarginAdj: 10, useMaxWidth: true, rightAngles: true,
  },
  startOnLoad: false,
})

hljs.registerLanguage('javascript', javascript); hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript); hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('python', python);         hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('xml', xml);               hljs.registerLanguage('html', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash);             hljs.registerLanguage('sh', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('yaml', yaml);             hljs.registerLanguage('yml', yaml)
hljs.registerLanguage('go', go)

const chatStore = useChatStore()
const listRef = ref<HTMLElement>()

// Throttled & Caching Setup
const htmlCache = new Map<string, string>()
const streamingHtml = ref('')

let lastStreamingMsgId = ''
let throttleTimeout: any = null
let pendingContent = ''
let isStreamingMsg = false // Module-level flag to identify current compiler state

// Session switch watch to clear caches
watch(
  () => chatStore.currentSessionId,
  () => {
    htmlCache.clear()
    if (throttleTimeout) {
      clearTimeout(throttleTimeout)
      throttleTimeout = null
    }
    lastStreamingMsgId = ''
    streamingHtml.value = ''
  }
)

watch(() => chatStore.currentMessages.length, scrollToBottom)
watch(() => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content, scrollToBottom)

// Watch the currentMessages for the active streaming message and update throttled streamingHtml
watch(
  () => chatStore.currentMessages,
  (newMessages) => {
    const lastMsg = newMessages[newMessages.length - 1]
    if (lastMsg && lastMsg.role === 'assistant' && lastMsg.isStreaming) {
      lastStreamingMsgId = lastMsg.id
      pendingContent = lastMsg.content

      if (!throttleTimeout) {
        // Immediately render the first chunk
        streamingHtml.value = renderContent(lastMsg)

        // Subsequently throttle updates to at most once every 60ms
        throttleTimeout = setTimeout(() => {
          throttleTimeout = null
          const msgToRender = chatStore.currentMessages.find(m => m.id === lastStreamingMsgId)
          if (msgToRender) {
            streamingHtml.value = renderContent(msgToRender)
          }
        }, 60)
      }
    } else {
      if (throttleTimeout) {
        clearTimeout(throttleTimeout)
        throttleTimeout = null
      }
      lastStreamingMsgId = ''
      streamingHtml.value = ''
    }
  },
  { deep: true }
)

function normalizeMarkdown(text: string): string {
  return text
    .replace(/([^\n])(\s*#{2,6}\s)/g, '$1\n\n$2')
    .replace(/([^\n])(\s*(?:[-*+]\s|(?:\d+\.)\s))/g, '$1\n$2')
    .replace(/([^\n])(\s*`{3,})/g, '$1\n$2')
    .replace(/(`{3,}[a-z]*\n?)([^\n])/g, '$1\n$2')
}

let mermaidIdCounter = 0
let currentRenderingMsgId = ''

const md = new MarkdownIt({
  html: false, linkify: true, breaks: false, typographer: true,
  highlight(str: string, lang: string): string {
    if (lang === 'mermaid') {
      if (isStreamingMsg) {
        // Standard light placeholder for Mermaid when streaming to prevent massive layout shifts & freezes
        return `<div class="mermaid-container mermaid-streaming">
          <div class="code-header">
            <span class="code-lang">mermaid diagram</span>
            <span class="code-line-count">Rendering...</span>
          </div>
          <pre class="mermaid-fallback">${md.utils.escapeHtml(str)}</pre>
        </div>`
      }
      const id = `mermaid-${++mermaidIdCounter}`
      const msgId = currentRenderingMsgId
      setTimeout(() => renderMermaid(msgId, id, str), 0)
      return `<div class="mermaid-container">
        <div class="mermaid-toolbar">
          <button class="mermaid-tool-btn zoom-btn" title="Zoom & Drag" type="button">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
            Zoom
          </button>
          <button class="mermaid-tool-btn export-png-btn" title="Export as High-Res PNG" type="button">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            PNG
          </button>
          <button class="mermaid-tool-btn export-svg-btn" title="Export as Vector SVG" type="button">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v8"/><path d="m8 12 4 4 4-4"/>
            </svg>
            SVG
          </button>
        </div>
        <div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(str)}</div>
      </div>`
    }
    const hl = (code: string, l: string) => {
      try { return hljs.highlight(code, { language: l, ignoreIllegals: true }).value } catch { return md.utils.escapeHtml(code) }
    }
    const detected = lang && hljs.getLanguage(lang) ? lang : (hljs.highlightAuto(str).language || '')
    const highlighted = detected ? hl(str, detected) : md.utils.escapeHtml(str)
    const langLabel = detected || 'text'
    const lines = str.split('\n').length
    return [
      `<div class="code-block">`,
      `<div class="code-header">`,
      `<span class="code-lang">${langLabel}</span>`,
      `<span class="code-line-count">${lines} lines</span>`,
      `<button class="code-copy-btn" type="button">Copy</button>`,
      `</div>`,
      `<code class="hljs">${highlighted}</code>`,
      `</div>`
    ].join('')
  }
})

function preprocessMermaid(definition: string): string {
  const lines = definition.split('\n')
  const classDefLines: string[] = []
  const otherLinesBeforeGraph: string[] = []
  let graphLineIdx = -1

  const graphTypeRegex = /^\s*(graph\s+|flowchart\s+|sequenceDiagram|gantt|classDiagram|stateDiagram|erDiagram|journey|pie|requirement|gitGraph|c4Diagram|mindmap|timeline|kanban|architecture|sankey|block)/i

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    if (line === undefined) continue
    if (graphTypeRegex.test(line)) {
      graphLineIdx = i
      break
    }
    const trimLine = line.trim()
    if (trimLine.startsWith('classDef') || 
        trimLine.startsWith('style ') || 
        trimLine.startsWith('class ') || 
        trimLine.startsWith('linkStyle ')) {
      classDefLines.push(line)
    } else {
      otherLinesBeforeGraph.push(line)
    }
  }

  if (graphLineIdx !== -1 && classDefLines.length > 0) {
    const result = [
      ...otherLinesBeforeGraph,
      lines[graphLineIdx] ?? '',
      ...classDefLines,
      ...lines.slice(graphLineIdx + 1)
    ]
    return result.join('\n')
  }

  return definition
}

async function renderMermaid(msgId: string, id: string, definition: string) {
  const el = document.getElementById(id)
  if (!el) return
  
  const preprocessed = preprocessMermaid(definition)
  
  try {
    const { svg } = await mermaid.render(id + '-svg', preprocessed)
    el.innerHTML = svg
    
    // Update the static HTML cache so that the fully compiled vector SVG
    // is preserved when Vue re-evaluates the getRenderedContent cache
    if (msgId) {
      const cachedHtml = htmlCache.get(msgId)
      if (cachedHtml) {
        const placeholder = `<div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(definition)}</div>`
        const replacement = `<div id="${id}" class="mermaid-svg">${svg}</div>`
        if (cachedHtml.includes(placeholder)) {
          htmlCache.set(msgId, cachedHtml.replace(placeholder, replacement))
        }
      }
    }
  } catch (err) {
    console.error('Mermaid render error:', err)
    el.innerHTML = `<pre class="mermaid-fallback">${md.utils.escapeHtml(definition)}</pre>`
  }
}

const defaultLinkOpen = md.renderer.rules.link_open ||
  ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.link_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const defaultTableOpen = md.renderer.rules.table_open ||
  ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.table_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return '<div class="table-scroll">' + defaultTableOpen(tokens, idx, options, env, self)
}
const defaultTableClose = md.renderer.rules.table_close ||
  ((_tokens: any, _idx: any, _options: any, _env: any, _self: any) => '</table>')
md.renderer.rules.table_close = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return defaultTableClose(tokens, idx, options, env, self) + '</div>'
}

function sanitizeIncomplete(text: string): string {
  const codeFenceCount = (text.match(/```/g) || []).length
  if (codeFenceCount % 2 !== 0) {
    // If the code fence count is odd, it means we are inside a code block.
    // We auto-close it by appending a newline and a closing fence block.
    return text + '\n```'
  }
  return text
}

function renderContent(msg: ChatMessage): string {
  isStreamingMsg = !!msg.isStreaming
  currentRenderingMsgId = msg.id
  const raw = msg.isStreaming ? sanitizeIncomplete(msg.content) : msg.content
  const normalized = normalizeMarkdown(raw)
  const html = md.render(normalized)
  currentRenderingMsgId = ''
  isStreamingMsg = false
  return msg.isStreaming ? html + '<span class="stream-cursor"></span>' : html
}

function getRenderedContent(msg: ChatMessage): string {
  if (htmlCache.has(msg.id)) {
    return htmlCache.get(msg.id)!
  }
  const html = renderContent(msg)
  htmlCache.set(msg.id, html)
  return html
}

function copyMessage(content: string, e: MouseEvent) {
  const btn = e.currentTarget as HTMLButtonElement
  navigator.clipboard.writeText(content).then(() => {
    btn.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg> Copied`
    setTimeout(() => { btn.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg> Copy` }, 1500)
  })
}

function scrollToBottom() {
  nextTick(() => { if (listRef.value) listRef.value.scrollTo({ top: listRef.value.scrollHeight, behavior: 'smooth' }) })
}

function handleCopyClick(e: MouseEvent) {
  const btn = (e.target as HTMLElement).closest<HTMLButtonElement>('.code-copy-btn')
  if (!btn) return
  const block = btn.closest('.code-block')
  const code = block?.querySelector('code')?.innerText ?? ''
  navigator.clipboard.writeText(code).then(() => {
    btn.textContent = 'Copied!'
    setTimeout(() => { btn.textContent = 'Copy' }, 1500)
  })
}

const activeZoomSvg = ref('')
const zoomScale = ref(1.0)
const panX = ref(0)
const panY = ref(0)

let isDragging = false
let startX = 0
let startY = 0

function handleMouseDown(e: MouseEvent) {
  if (e.button !== 0) return // Only drag on left click
  isDragging = true
  startX = e.clientX - panX.value
  startY = e.clientY - panY.value
  
  const viewport = e.currentTarget as HTMLElement
  viewport.style.cursor = 'grabbing'
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging) return
  panX.value = e.clientX - startX
  panY.value = e.clientY - startY
}

function handleMouseUp(e: MouseEvent) {
  isDragging = false
  const viewport = e.currentTarget as HTMLElement
  if (viewport) {
    viewport.style.cursor = 'grab'
  }
}

function resetZoom() {
  zoomScale.value = 1.0
  panX.value = 0
  panY.value = 0
}

// 缩放步长和范围
const ZOOM_STEP = 0.1
const ZOOM_MIN = 0.25
const ZOOM_MAX = 5

function handleWheel(e: WheelEvent) {
  // 以鼠标光标位置为缩放中心
  const viewport = e.currentTarget as HTMLElement
  const rect = viewport.getBoundingClientRect()

  // 鼠标相对于视口中心的偏移
  const mouseX = e.clientX - rect.left - rect.width / 2
  const mouseY = e.clientY - rect.top - rect.height / 2

  const prevScale = zoomScale.value
  const delta = e.deltaY < 0 ? ZOOM_STEP : -ZOOM_STEP
  const newScale = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, prevScale + delta))

  // 调整 pan 以保证缩放中心固定在鼠标位置
  const scaleDiff = newScale / prevScale
  panX.value = mouseX - scaleDiff * (mouseX - panX.value)
  panY.value = mouseY - scaleDiff * (mouseY - panY.value)

  zoomScale.value = newScale
}

function closeZoom() {
  activeZoomSvg.value = ''
  resetZoom()
}

function handleMermaidClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  
  // Handle Zoom Button
  const zoomBtn = target.closest<HTMLButtonElement>('.zoom-btn')
  if (zoomBtn) {
    const container = zoomBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      // Defensive fallback: find the child svg that doesn't belong to the toolbar
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s
          break
        }
      }
    }
    if (svgEl) {
      activeZoomSvg.value = svgEl.outerHTML
      resetZoom()
    }
    return
  }

  // Handle Export PNG Button
  const exportPngBtn = target.closest<HTMLButtonElement>('.export-png-btn')
  if (exportPngBtn) {
    const container = exportPngBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s
          break
        }
      }
    }
    if (svgEl) {
      triggerPngDownload(svgEl.outerHTML, 'mermaid-diagram.png')
    }
    return
  }

  // Handle Export SVG Button
  const exportSvgBtn = target.closest<HTMLButtonElement>('.export-svg-btn')
  if (exportSvgBtn) {
    const container = exportSvgBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s
          break
        }
      }
    }
    if (svgEl) {
      triggerSvgDownload(svgEl.outerHTML, 'mermaid-diagram.svg')
    }
    return
  }
}

function triggerSvgDownload(svgContent: string, filename: string) {
  const blob = new Blob([svgContent], { type: 'image/svg+xml;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function triggerPngDownload(svgContent: string, filename: string) {
  const img = new Image()
  const svgBlob = new Blob([svgContent], { type: 'image/svg+xml;charset=utf-8' })
  const url = URL.createObjectURL(svgBlob)
  
  img.onload = () => {
    const canvas = document.createElement('canvas')
    const scaleFactor = 2 // Generate high-def 2x resolution PNG
    const originalWidth = img.naturalWidth || 800
    const originalHeight = img.naturalHeight || 600
    
    canvas.width = originalWidth * scaleFactor
    canvas.height = originalHeight * scaleFactor
    
    const ctx = canvas.getContext('2d')
    if (ctx) {
      // Draw solid white canvas background (avoiding pitch black transparencies in Word/PPT/WeChat)
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      
      ctx.scale(scaleFactor, scaleFactor)
      ctx.drawImage(img, 0, 0)
      
      try {
        const pngUrl = canvas.toDataURL('image/png')
        const a = document.createElement('a')
        a.href = pngUrl
        a.download = filename
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
      } catch (err) {
        console.error('Failed to export canvas to PNG:', err)
        triggerSvgDownload(svgContent, filename.replace('.png', '.svg'))
      }
    }
    URL.revokeObjectURL(url)
  }
  
  img.onerror = () => {
    triggerSvgDownload(svgContent, filename.replace('.png', '.svg'))
    URL.revokeObjectURL(url)
  }
  
  img.src = url
}

function downloadModalSvg() {
  if (activeZoomSvg.value) {
    triggerSvgDownload(activeZoomSvg.value, 'mermaid-diagram.svg')
  }
}

function downloadModalPng() {
  if (activeZoomSvg.value) {
    triggerPngDownload(activeZoomSvg.value, 'mermaid-diagram.png')
  }
}

onMounted(() => {
  if (!listRef.value) return
  listRef.value.addEventListener('click', handleCopyClick)
  listRef.value.addEventListener('click', handleMermaidClick)
})

onUnmounted(() => {
  if (listRef.value) {
    listRef.value.removeEventListener('click', handleCopyClick)
    listRef.value.removeEventListener('click', handleMermaidClick)
  }
})
</script>

<style scoped>
.msg-scroll {
  height: 100%;
  overflow-y: auto;
  padding: var(--spacing-24) 0;
  scroll-behavior: smooth;
}

/* ── Welcome Whiteboard Card ─────────────────────────────────── */
.welcome {
  max-width: var(--chat-max-width);
  margin: 0 auto;
  padding: var(--spacing-32) var(--spacing-24);
}

.welcome-card {
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03), inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-soft-fog);
  border-bottom: 1px solid var(--color-cloud-cover);
  padding: 10px 20px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-success-green);
}

.status-text {
  font-size: 10px;
  color: var(--color-cool-graphite);
  letter-spacing: 0.12em;
  font-weight: 500;
}

.welcome-body {
  padding: var(--spacing-32) var(--spacing-24);
}

.welcome-title {
  font-family: var(--font-gelica);
  font-size: 32px;
  font-weight: 600;
  color: var(--color-midnight-ink);
  margin-bottom: var(--spacing-12);
  display: flex;
  align-items: center;
  gap: var(--spacing-12);
  line-height: 1.11;
  letter-spacing: normal;
}

.welcome-icon {
  color: var(--color-success-green);
  font-size: 24px;
}

.welcome-sub {
  font-size: 16px;
  color: var(--color-cool-graphite);
  line-height: 1.6;
  max-width: 600px;
  margin-bottom: var(--spacing-24);
}

.welcome-divider {
  height: 1px;
  background: radial-gradient(circle at left, var(--color-cloud-cover) 0%, transparent 100%);
  margin-bottom: var(--spacing-24);
}

.actions-label {
  display: block;
  font-size: 10px;
  color: var(--color-icon-stroke);
  margin-bottom: var(--spacing-12);
  letter-spacing: 0.08em;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--spacing-12);
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  padding: 18px;
  background: var(--color-soft-fog);
  border: 1px solid var(--color-cloud-cover);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.quick-card:hover {
  border-color: var(--color-success-green);
  background: var(--color-canvas-white);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(84, 177, 108, 0.08);
}

.card-tag {
  font-size: 9px;
  font-weight: 600;
  color: var(--color-success-green);
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.card-title {
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-midnight-ink);
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: var(--color-cool-graphite);
  line-height: 1.4;
}

/* ── Thread ──────────────────────────────────────────────── */
.thread {
  max-width: var(--chat-max-width);
  margin: 0 auto;
  padding: 0 var(--spacing-24);
}

.msg-block {
  margin-bottom: var(--spacing-16);
  will-change: transform, opacity;
}

/* Msg Animations */
.msg-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.msg-fade-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-16px);
}
.msg-fade-move {
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

/* User Bubble */
.user-msg {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-12);
  padding: 14px 18px;
  background: var(--color-pale-ash);
  border: 1px solid var(--color-cloud-cover);
  border-radius: var(--radius-md);
  margin-left: 10%;
}

.msg-prompt {
  color: var(--color-success-green);
  font-weight: 600;
  flex-shrink: 0;
  margin-top: 2px;
}

.user-text {
  white-space: pre-wrap;
  color: var(--color-midnight-ink);
  font-size: 14px;
  line-height: 1.5;
}

/* AI Msg */
.ai-msg {
  padding: var(--spacing-12) 0 var(--spacing-24);
  margin-right: 5%;
}

.msg-footer {
  display: flex;
  gap: var(--spacing-8);
  margin-top: var(--spacing-12);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.msg-block:hover .msg-footer {
  opacity: 1;
}

.copy-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-family: var(--font-sans);
  font-size: 11px;
  color: var(--color-cool-graphite);
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn:hover {
  color: var(--color-midnight-ink);
  border-color: var(--color-pale-stone);
  background: var(--color-soft-fog);
}

/* ── Streaming Cursor ────────────────────────────────────── */
.markdown-body :deep(.stream-cursor) {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  background: var(--color-success-green);
  margin-left: 1px;
  vertical-align: text-bottom;
  animation: blink 0.9s steps(2) infinite;
}

/* ── Markdown Body ───────────────────────────────────────── */

.markdown-body {
  font-size: var(--text-body-sm);
  line-height: 1.75;
  color: var(--color-midnight-ink);
  word-break: break-word;
}

/* Paragraphs */
.markdown-body :deep(p) {
  margin: 0 0 12px;
}
.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

/* Headings */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  font-family: var(--font-gelica);
  font-weight: 600;
  color: var(--color-midnight-ink);
  margin: 24px 0 10px;
  line-height: 1.11;
  letter-spacing: normal;
}
.markdown-body :deep(h2) { font-size: 22px; margin-top: 28px; border-bottom: 1px solid var(--color-cloud-cover); padding-bottom: 6px; }
.markdown-body :deep(h3) { font-size: 18px; }
.markdown-body :deep(h4) { font-size: 16px; }
.markdown-body :deep(h5),
.markdown-body :deep(h6) { font-size: 14px; }

.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child),
.markdown-body :deep(h4:first-child) { margin-top: 0; }

/* Horizontal rule */
.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-cloud-cover);
  margin: 20px 0;
}

/* Blockquote */
.markdown-body :deep(blockquote) {
  margin: 12px 0;
  padding: 10px 16px;
  border-left: 3px solid var(--color-success-green);
  background: var(--color-soft-fog);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-cool-graphite);
}
.markdown-body :deep(blockquote p) { margin: 0; }

/* Lists */
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 22px;
  margin: 8px 0 12px;
}
.markdown-body :deep(li) {
  margin-bottom: 6px;
}
.markdown-body :deep(li > ul),
.markdown-body :deep(li > ol) {
  margin: 4px 0;
}

/* Inline code */
.markdown-body :deep(code) {
  font-family: var(--font-mono);
  font-size: 13px;
  background: var(--color-soft-fog);
  color: #d63384;
  padding: 1px 6px;
  border-radius: 3px;
  border: 1px solid var(--color-cloud-cover);
}

/* ── Code Blocks (Premium Whiteboard Style) ─────────────────── */
.markdown-body :deep(.code-block) {
  background: #fafafa;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin: 14px 0;
  border: 1px solid var(--color-cloud-cover);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.01);
}
.markdown-body :deep(.code-block:hover) {
  border-color: var(--color-pale-stone);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}
.markdown-body :deep(.code-header) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--color-pale-ash);
  border-bottom: 1px solid var(--color-cloud-cover);
  user-select: none;
}
.markdown-body :deep(.code-lang) {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  color: var(--color-success-green);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.markdown-body :deep(.code-line-count) {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-icon-stroke);
  margin-right: auto;
}
.markdown-body :deep(.code-copy-btn) {
  font-family: var(--font-sans);
  font-size: 11px;
  color: var(--color-cool-graphite);
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  transition: all 0.15s;
  opacity: 0;
}
.markdown-body :deep(.code-block:hover .code-copy-btn) {
  opacity: 1;
}
.markdown-body :deep(.code-copy-btn:hover) {
  color: var(--color-midnight-ink);
  border-color: var(--color-pale-stone);
  background: var(--color-soft-fog);
}
.markdown-body :deep(.code-block code.hljs) {
  display: block;
  background: transparent;
  color: #383a42;
  padding: 14px 18px;
  font-size: 13px;
  line-height: 1.65;
  overflow-x: auto;
  border: none;
  border-radius: 0;
  tab-size: 2;
}
.markdown-body :deep(pre:not(.code-block)) {
  background: #fafafa;
  color: #383a42;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid var(--color-cloud-cover);
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.6;
}
.markdown-body :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
  border: none;
  border-radius: 0;
  font-size: inherit;
}

/* ── Syntax Tokens — Clean Light Theme ──────────────────────── */
.markdown-body :deep(.hljs-keyword)    { color: #a626a4; font-weight: 600; }
.markdown-body :deep(.hljs-built_in)   { color: #0184a7; }
.markdown-body :deep(.hljs-type)       { color: #c18401; }
.markdown-body :deep(.hljs-literal)    { color: #0184a7; }
.markdown-body :deep(.hljs-number)     { color: #986801; }
.markdown-body :deep(.hljs-string)     { color: #50a14f; }
.markdown-body :deep(.hljs-comment)    { color: #a0a1a7; font-style: italic; }
.markdown-body :deep(.hljs-function)   { color: #4078f2; }
.markdown-body :deep(.hljs-title)      { color: #4078f2; font-weight: 500; }
.markdown-body :deep(.hljs-attr)       { color: #986801; }
.markdown-body :deep(.hljs-variable)   { color: #e06c75; }
.markdown-body :deep(.hljs-tag)        { color: #e45649; }
.markdown-body :deep(.hljs-name)       { color: #e45649; }
.markdown-body :deep(.hljs-meta)       { color: #4078f2; }
.markdown-body :deep(.hljs-operator)   { color: #383a42; }
.markdown-body :deep(.hljs-params)     { color: #383a42; }

/* ── Links ────────────────────────────────────────────────── */
.markdown-body :deep(a) {
  color: var(--color-accent);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.12s;
}
.markdown-body :deep(a:hover) { border-bottom-color: var(--color-accent); }

.markdown-body :deep(strong) { color: var(--color-midnight-ink); font-weight: 600; }
.markdown-body :deep(em) { font-style: italic; color: var(--color-cool-graphite); }

/* ── Tables ───────────────────────────────────────────────── */
.markdown-body :deep(.table-scroll) {
  width: 100%; overflow-x: auto; margin: 14px 0;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-cloud-cover);
}
.markdown-body :deep(table) {
  width: 100%; border-collapse: collapse;
  font-size: 13px; min-width: 400px;
}
.markdown-body :deep(thead) { background: var(--color-pale-ash); }
.markdown-body :deep(th) {
  padding: 9px 14px; text-align: left; font-weight: 600;
  font-size: 12px; color: var(--color-cool-graphite);
  border-bottom: 1px solid var(--color-cloud-cover);
  white-space: nowrap;
}
.markdown-body :deep(td) {
  padding: 9px 14px; border-bottom: 1px solid var(--color-cloud-cover);
  color: var(--color-midnight-ink);
}
.markdown-body :deep(tr:last-child td) { border-bottom: none; }
.markdown-body :deep(tr:nth-child(even)) { background: var(--color-soft-fog); }

/* ── Images ───────────────────────────────────────────────── */
.markdown-body :deep(img) {
  max-width: 100%; border-radius: var(--radius-sm);
  margin: 12px 0; border: 1px solid var(--color-cloud-cover);
}

/* ── Mermaid Diagrams ──────────────────────────────────────── */
.markdown-body :deep(.mermaid-container) {
  position: relative;
  margin: 14px 0;
  padding: 20px;
  background: #ffffff;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-cloud-cover);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  transition: box-shadow 0.2s ease;
}
.markdown-body :deep(.mermaid-container:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05), 0 1px 4px rgba(0, 0, 0, 0.03);
}
.markdown-body :deep(.mermaid-toolbar) {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 5;
}
.markdown-body :deep(.mermaid-container:hover .mermaid-toolbar) {
  opacity: 1;
}
.markdown-body :deep(.mermaid-tool-btn) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 500;
  color: var(--color-cool-graphite);
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  transition: all 0.2s;
}
.markdown-body :deep(.mermaid-tool-btn:hover) {
  color: var(--color-midnight-ink);
  border-color: var(--color-pale-stone);
  background: var(--color-soft-fog);
  transform: translateY(-1px);
}
.markdown-body :deep(.mermaid-svg) {
  display: flex;
  justify-content: center;
  overflow-x: auto;
}
.markdown-body :deep(.mermaid-svg svg) {
  max-width: 100%;
  height: auto;
  font-family: Inter, -apple-system, BlinkMacSystemFont, sans-serif;
}
.markdown-body :deep(.mermaid-fallback) {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--color-cool-graphite);
  white-space: pre-wrap;
}

/* ── Zoom Modal ─────────────────────────────────────────────── */
.zoom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(10, 10, 10, 0.35);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.zoom-modal-content {
  width: 90vw;
  height: 85vh;
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: modalScaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes modalScaleUp {
  from { opacity: 0; transform: scale(0.96); }
  to   { opacity: 1; transform: scale(1); }
}

.zoom-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  background: var(--color-pale-ash);
  border-bottom: 1px solid var(--color-cloud-cover);
}

.zoom-modal-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  font-family: var(--font-sans);
  font-size: 12px;
  font-weight: 500;
  color: var(--color-cool-graphite);
  background: var(--color-canvas-white);
  border: 1px solid var(--color-cloud-cover);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}
.modal-btn:hover {
  color: var(--color-midnight-ink);
  border-color: var(--color-pale-stone);
  background: var(--color-soft-fog);
}
.export-action {
  color: var(--color-canvas-white);
  background: var(--color-steel-gray);
  border-color: var(--color-steel-gray);
}
.export-action:hover {
  color: var(--color-canvas-white);
  background: var(--color-midnight-ink);
  border-color: var(--color-midnight-ink);
}

.scale-label {
  font-size: 12px;
  color: var(--color-cool-graphite);
  min-width: 48px;
  text-align: center;
}

.modal-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--color-icon-stroke);
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s;
}
.modal-close-btn:hover {
  background: var(--color-cloud-cover);
  color: var(--color-midnight-ink);
}

.zoom-viewport {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--color-soft-fog);
  background-image: radial-gradient(rgba(0, 0, 0, 0.03) 1.2px, transparent 1.2px);
  background-size: 24px 24px;
}

.zoom-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  transform-origin: center center;
  width: 100%;
  height: 100%;
}

.zoom-wrapper :deep(svg) {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
}

/* Modal fade animations */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
