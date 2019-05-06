package com.blind3r.photomanager.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.blind3r.photomanager.entities.Photo;

@Repository
public interface PhotoRepository extends CrudRepository<Photo, Long>{}
