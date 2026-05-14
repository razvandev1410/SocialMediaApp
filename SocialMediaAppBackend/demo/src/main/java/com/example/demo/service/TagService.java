package com.example.demo.service;

import com.example.demo.entity.Tag;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class TagService {
    @Autowired
    public TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getOrCreateTag(String name) {
        Optional<Tag> tagOptional = tagRepository.findByNameIgnoreCase(name.trim());
        if(tagOptional.isPresent())
            return tagOptional.get();
        else {
            Tag tag = new Tag();
            tag.setName(name.trim().toLowerCase());
            return tagRepository.save(tag);
        }
    }

    public Tag getTagById(Long id) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        if(tagOptional.isPresent())
            return tagOptional.get();
        else
            throw new ResourceNotFoundException("Tag with id " + id + " not found!");
    }

    public void deleteTag(Long id) {
        if(!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag with id " + id + " not found!");
        }
        tagRepository.deleteById(id);
    }
}
