package com.example.product_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.product_service.entity.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private Cloudinary cloudinary;

    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {
        // 1. Upload the image to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
        
        // 2. Extract the URL from the result
        String imageUrl = uploadResult.get("url").toString();
        
        // 3. Set the URL to the product object
        product.setImageUrl(imageUrl);
        
        // 4. Save to Database
        return repository.save(product);
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }
}