package com.school.manage.controller;

import com.school.manage.model.TutorialVideo;
import com.school.manage.model.User;
import com.school.manage.service.TutorialVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TutorialVideoController {

    private final TutorialVideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<TutorialVideo> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("subject") String subject,
            @RequestParam("className") String className,
            @RequestParam(value = "chapter", required = false) String chapter,
            Authentication auth) throws Exception {

        User user = (User) auth.getPrincipal();

        TutorialVideo video = new TutorialVideo();
        video.setTitle(title);
        video.setDescription(description);
        video.setSubject(subject);
        video.setClassName(className);
        video.setChapter(chapter);
        video.setTeacherId(user.getId());
        video.setTeacherName(user.getFullName());

        log.info("[TutorialVideoController] POST /api/videos — teacher='{}', title='{}', class='{}'",
                user.getFullName(), title, className);
        return new ResponseEntity<>(videoService.upload(file, video), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<List<TutorialVideo>> getAll(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String subject) {
        if (className != null && subject != null) {
            return ResponseEntity.ok(videoService.getByClassAndSubject(className, subject));
        }
        if (className != null) {
            return ResponseEntity.ok(videoService.getByClassName(className));
        }
        return ResponseEntity.ok(videoService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TutorialVideo> getById(@PathVariable String id) {
        return ResponseEntity.ok(videoService.getById(id));
    }

    @GetMapping("/{id}/stream")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> streamVideo(@PathVariable String id) {
        TutorialVideo video = videoService.getById(id);
        InputStream inputStream = videoService.streamVideo(id);

        StreamingResponseBody responseBody = outputStream -> {
            byte[] buffer = new byte[8192];
            int bytesRead;
            try (inputStream) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, video.getContentType() != null
                        ? video.getContentType() : "video/mp4")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getFileName() + "\"")
                .body(responseBody);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
