package com.school.manage.service;

import com.school.manage.exception.ResourceNotFoundException;
import com.school.manage.model.Student;
import com.school.manage.model.TutorialVideo;
import com.school.manage.repository.StudentRepository;
import com.school.manage.repository.TutorialVideoRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorialVideoService {

    private final TutorialVideoRepository videoRepository;
    private final StudentRepository studentRepository;
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    public TutorialVideo upload(MultipartFile file, TutorialVideo video) throws IOException {
        // Store file in GridFS
        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );

        video.setGridFsFileId(fileId.toString());
        video.setFileName(file.getOriginalFilename());
        video.setContentType(file.getContentType());
        video.setFileSize(file.getSize());
        video.setCreatedAt(LocalDateTime.now());

        log.info("[TutorialVideoService] Uploaded video '{}' ({} MB) for class {} - {}",
                video.getTitle(), file.getSize() / (1024 * 1024),
                video.getClassName(), video.getSubject());

        return videoRepository.save(video);
    }

    public InputStream streamVideo(String videoId) {
        TutorialVideo video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + videoId));

        GridFSFile gridFsFile = gridFsTemplate.findOne(
                new Query(Criteria.where("_id").is(new ObjectId(video.getGridFsFileId())))
        );
        if (gridFsFile == null) {
            throw new ResourceNotFoundException("Video file not found in storage");
        }

        try {
            return gridFsOperations.getResource(gridFsFile).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Failed to stream video", e);
        }
    }

    public TutorialVideo getById(String id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));
    }

    public List<TutorialVideo> getAll() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<TutorialVideo> getByClassName(String className) {
        return videoRepository.findByClassNameAndStatusOrderByCreatedAtDesc(className, "ACTIVE");
    }

    public List<TutorialVideo> getByClassAndSubject(String className, String subject) {
        return videoRepository.findByClassNameAndSubjectAndStatusOrderByCreatedAtDesc(
                className, subject, "ACTIVE");
    }

    public List<TutorialVideo> getByTeacher(String teacherId) {
        return videoRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    public List<TutorialVideo> getVideosForStudent(String studentEntityId) {
        Student student = studentRepository.findById(studentEntityId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEntityId));
        String className = student.getClassForAdmission();
        log.info("[TutorialVideoService] Fetching videos for student {} in class {}", studentEntityId, className);
        return videoRepository.findByClassNameAndStatusOrderByCreatedAtDesc(className, "ACTIVE");
    }

    public void delete(String id) {
        TutorialVideo video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));

        // Delete from GridFS
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(video.getGridFsFileId()))));

        // Delete metadata
        videoRepository.deleteById(id);
        log.info("[TutorialVideoService] Deleted video: {}", id);
    }
}
