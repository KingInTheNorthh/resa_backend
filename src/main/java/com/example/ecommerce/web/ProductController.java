package com.example.ecommerce.web;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductImage;
import com.example.ecommerce.service.AppUserService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.web.dto.ProductRequest;
import com.example.ecommerce.web.dto.ProductResponse;
import com.example.ecommerce.web.dto.ProductImageResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final AppUserService appUserService;

    public ProductController(ProductService productService, AppUserService appUserService) {
        this.productService = productService;
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        List<Product> products = productService.listProducts();
        if (products.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ProductImage>> imagesByProductId = productService.listImagesByProductIds(
            products.stream().map(Product::getId).collect(Collectors.toList())
        );
        return products.stream()
            .map(product -> toResponse(product, imagesByProductId.get(product.getId())))
            .collect(Collectors.toList());
    }



    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return toResponse(product, productService.listImages(id));
    }



    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER') or hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestPart("product") ProductRequest request,
                                                         @RequestPart("images") List<MultipartFile> images,
                                                         Authentication authentication) {
        AppUser seller = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Product product = productService.createProduct(request, seller, images);
        ProductResponse response = toResponse(product, productService.listImages(product.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER') or hasRole('SELLER')")
    public ProductResponse updateProduct(@PathVariable Long id,
                                         @Valid @RequestPart("product") ProductRequest request,
                                         @RequestPart("images") List<MultipartFile> images,
                                         Authentication authentication) {
        AppUser seller = appUserService.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Product product = productService.updateProduct(id, request, seller, images);
        return toResponse(product, productService.listImages(product.getId()));
    }




    @GetMapping("/images/{imageId}")
    public ResponseEntity<Resource> getImage(@PathVariable Long imageId) {
        ProductImage image = productService.getImage(imageId);
        Resource resource = productService.getImageResource(image);
        long lastModified = image.getCreatedAt().toEpochMilli();
        String etag = "\"" + image.getId() + "-" + lastModified + "-" + image.getSize() + "\"";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .eTag(etag)
            .lastModified(lastModified)
            .body(resource);
    }



    private ProductResponse toResponse(Product product, List<ProductImage> images) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        if (images == null || images.isEmpty()) {
            response.setImages(List.of());
            return response;
        }
        response.setImages(images.stream()
            .map(image -> new ProductImageResponse(image.getId(), "/api/products/images/" + image.getId()))
            .collect(Collectors.toList()));
        return response;
    }
}
