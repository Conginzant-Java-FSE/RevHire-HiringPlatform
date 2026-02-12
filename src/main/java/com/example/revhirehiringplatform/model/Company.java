//package com.example.revhirehiringplatform.model;
//
//
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "companies")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//public class Company {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Removed logic where Company had direct User relation (User is now linked via
//    // EmployerProfile)
//    // @OneToOne
//    // @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
//    // private User user;
//
//    private String name;
//    private String industry;
//    private String size;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    private String website;
//    private String location;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//}

package com.example.revhirehiringplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Removed logic where Company had direct User relation (User is now linked via
    // EmployerProfile)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    private User createdBy;

    private String name;
    private String industry;
    private String size;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;
    private String location;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}