package eidd.grp5.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    
    T save(T entity);
    
    List<T> findAll();
    
    Optional<T> findById(Long id);
    
    boolean delete(Long id);
    
    long count();
}
