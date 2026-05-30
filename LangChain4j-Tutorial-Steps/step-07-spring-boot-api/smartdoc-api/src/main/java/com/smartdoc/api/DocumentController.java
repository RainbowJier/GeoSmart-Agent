package com.smartdoc.api;

import com.smartdoc.rag.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档管理 REST 控制器
 *
 * 【Step 06 对照】Step 06 中文档摄入是在 main() 启动时一次性完成的：
 *
 *   // Step 06 的写法：
 *   Document document = FileSystemDocumentLoader.loadDocument(Path.of("doc.txt"), parser);
 *   ingestor.ingest(document);
 *
 * Spring Boot 方式：通过 REST API 暴露文档上传端点，
 * 用户可以在运行时动态上传文档到知识库。
 *
 * 端点：
 * - POST /api/documents/upload → 上传文档（PDF/DOCX/TXT）
 * - GET  /api/documents        → 列出已上传文档
 *
 * 文档处理流程：
 * 1. 接收上传的 MultipartFile
 * 2. 保存为临时文件
 * 3. 调用 DocumentIngestionService 进行解析、分块、向量化、存储
 * 4. 删除临时文件
 * 5. 返回处理结果
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    /** 文档摄入服务（由 smartdoc-rag 模块提供） */
    private final DocumentIngestionService ingestionService;

    /** 已上传文档列表（内存中维护，重启后清空） */
    private final List<String> uploadedDocuments = new ArrayList<>();

    /**
     * 上传并摄入文档
     *
     * 【Step 06 对照】Step 06 中文档是预先放在项目目录中的。
     * Spring Boot 方式支持运行时动态上传，无需重启服务。
     *
     * 支持 multipart/form-data 格式的文件上传。
     * 文件类型由 DocumentIngestionService 自动检测。
     *
     * @param file 上传的文件（PDF/DOCX/TXT）
     * @return 上传结果（包含文件名和状态）
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("doc-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());

            ingestionService.ingestDocument(tempFile);

            uploadedDocuments.add(file.getOriginalFilename());
            log.info("Uploaded and ingested: {}", file.getOriginalFilename());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "filename", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }

    /**
     * 列出所有已上传的文档
     *
     * @return 文档列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listDocuments() {
        return ResponseEntity.ok(Map.of("documents", uploadedDocuments));
    }
}
