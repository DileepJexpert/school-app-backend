package com.school.manage.controller;

import com.school.manage.model.Gallery;
import com.school.manage.service.GalleryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<List<Gallery>> getAll() {
        return ResponseEntity.ok(galleryService.getAll());
    }

    @GetMapping("/published")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Gallery>> getPublished() {
        return ResponseEntity.ok(galleryService.getPublished());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Gallery> getById(@PathVariable String id) {
        return ResponseEntity.ok(galleryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Gallery> create(@RequestBody Gallery gallery, Authentication authentication) {
        gallery.setUploadedBy(authentication.getName());
        log.info("[GalleryController] POST /api/gallery — title='{}', uploadedBy='{}'",
                gallery.getTitle(), gallery.getUploadedBy());
        return new ResponseEntity<>(galleryService.create(gallery), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Gallery> update(@PathVariable String id, @RequestBody Gallery gallery) {
        log.info("[GalleryController] PUT /api/gallery/{}", id);
        return ResponseEntity.ok(galleryService.update(id, gallery));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("[GalleryController] DELETE /api/gallery/{}", id);
        galleryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Gallery> addImages(
            @PathVariable String id,
            @RequestBody List<Gallery.GalleryImage> images) {
        log.info("[GalleryController] POST /api/gallery/{}/images — count={}", id, images.size());
        return ResponseEntity.ok(galleryService.addImages(id, images));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Gallery> removeImage(
            @PathVariable String id,
            @PathVariable String imageId) {
        log.info("[GalleryController] DELETE /api/gallery/{}/images/{}", id, imageId);
        return ResponseEntity.ok(galleryService.removeImage(id, imageId));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Gallery>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(galleryService.getByCategory(category));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(galleryService.getStats());
    }
}
