package com.scholartrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone cannot be empty")
    private String phone;

    @NotNull(message = "Marks cannot be null")
    @PositiveOrZero(message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Double marks;

    @NotNull(message = "Annual income cannot be null")
    @PositiveOrZero(message = "Annual income cannot be negative")
    private Double annualIncome;

    @NotBlank(message = "Course cannot be empty")
    private String course;

    @NotBlank(message = "College cannot be empty")
    private String college;

    public Student() {
    }

    public Student(Long id, String name, String email, String phone, Double marks, Double annualIncome, String course, String college) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.marks = marks;
        this.annualIncome = annualIncome;
        this.course = course;
        this.college = college;
    }

    public Student(String name, String email, String phone, Double marks, Double annualIncome, String course, String college) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.marks = marks;
        this.annualIncome = annualIncome;
        this.course = course;
        this.college = college;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public Double getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(Double annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }
}
