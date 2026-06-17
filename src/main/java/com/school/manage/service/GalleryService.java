package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Gallery;
import com.school.manage.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public Gallery create(Gallery gallery) {
        gallery.setCreatedAt(LocalDateTime.now());
        if (gallery.getImages() != null) {
            gallery.getImages().forEach(img -> {
                if (img.getId() == null) {
                    img.setId(UUID.randomUUID().toString());
                }
            });
        }
        return galleryRepository.save(gallery);
    }

    public Gallery update(String id, Gallery gallery) {
        Gallery existing = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + id));
        gallery.setId(existing.getId());
        gallery.setCreatedAt(existing.getCreatedAt());
        gallery.setUpdatedAt(LocalDateTime.now());
        return galleryRepository.save(gallery);
    }

    public void delete(String id) {
        if (!galleryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gallery not found with id: " + id);
        }
        galleryRepository.deleteById(id);
    }

    public List<Gallery> getAll() {
        return galleryRepository.findAllByOrderByDateDesc();
    }

    public List<Gallery> getPublished() {
        return galleryRepository.findByPublishedTrueOrderByDateDesc();
    }

    public Gallery getById(String id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + id));
    }

    public List<Gallery> getByCategory(String category) {
        return galleryRepository.findByCategory(category);
    }

    public Gallery addImages(String galleryId, List<Gallery.GalleryImage> newImages) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + galleryId));

        newImages.forEach(img -> {
            if (img.getId() == null) {
                img.setId(UUID.randomUUID().toString());
            }
        });

        if (gallery.getImages() == null) {
            gallery.setImages(new ArrayList<>());
        }
        gallery.getImages().addAll(newImages);
        gallery.setUpdatedAt(LocalDateTime.now());
        return galleryRepository.save(gallery);
    }

    public Gallery removeImage(String galleryId, String imageId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + galleryId));

        if (gallery.getImages() != null) {
            gallery.setImages(gallery.getImages().stream()
                    .filter(img -> !imageId.equals(img.getId()))
                    .collect(Collectors.toList()));
        }
        gallery.setUpdatedAt(LocalDateTime.now());
        return galleryRepository.save(gallery);
    }

    public Map<String, Object> getStats() {
        List<Gallery> allGalleries = galleryRepository.findAll();

        int totalAlbums = allGalleries.size();
        int totalImages = allGalleries.stream()
                .mapToInt(g -> g.getImages() != null ? g.getImages().size() : 0)
                .sum();

        Map<String, Long> categoryBreakdown = allGalleries.stream()
                .filter(g -> g.getCategory() != null)
                .collect(Collectors.groupingBy(Gallery::getCategory, Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAlbums", totalAlbums);
        stats.put("totalImages", totalImages);
        stats.put("categoryBreakdown", categoryBreakdown);
        return stats;
    }
}
