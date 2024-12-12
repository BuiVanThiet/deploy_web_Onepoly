package com.example.shopgiayonepoly.repositores;

import com.example.shopgiayonepoly.entites.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Repository
public interface CustomerRegisterRepository extends JpaRepository<Customer,Integer> {
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Customer c WHERE c.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Customer c WHERE c.acount = :acount")
    boolean existsByAcount(@Param("acount") String acount);

    @Query("SELECT c FROM Customer c WHERE c.acount = :acount")
    Customer findByAcount(@Param("acount") String acount);
    //doi mk
    @Query("select c from Customer c where c.email = :emailCheck")
    Customer getCustomerByEmail(@Param("emailCheck") String email);

}
