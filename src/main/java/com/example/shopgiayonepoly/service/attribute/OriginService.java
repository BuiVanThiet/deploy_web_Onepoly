package com.example.shopgiayonepoly.service.attribute;

import com.example.shopgiayonepoly.entites.Origin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface OriginService {
    List<Origin> findAll();

    <S extends Origin> S save(S entity);

    Optional<Origin> findById(Integer integer);

    long count();

    void deleteByID(int id);

    List<Origin> findAll(Sort sort);

    Page<Origin> findAll(Pageable pageable);

    List<Origin> getOriginNotStatus0();

    List<Origin> getOriginDelete();

    void updateStatus(int id, int status);

    void updateOrigin(int id, String codeOrigin, String nameOrigin);

}
