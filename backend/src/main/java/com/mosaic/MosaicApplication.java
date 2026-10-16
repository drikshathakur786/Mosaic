package com.mosaic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mosaic Application Entry Point.
 *
 * Mosaic is a privacy-first personal finance clarity platform that helps
 * young adults understand where their money goes through transaction
 * categorization, recurring payment detection, shared expense tracking,
 * and evidence-backed AI insights.
 */
@SpringBootApplication
public class MosaicApplication {

    public static void main(String[] args) {
        SpringApplication.run(MosaicApplication.class, args);
    }
}
