package com.springcrudfile.SpringCrudApp.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springcrudfile.SpringCrudApp.model.ProductDto;
import com.springcrudfile.SpringCrudApp.model.Products;
import com.springcrudfile.SpringCrudApp.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {
	
	@Autowired
	private ProductsRepository repo;
	
	@GetMapping({"","/"})
	public String showProductList(Model model) {
		List<Products> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/CreateProduct";
	}
	
	@PostMapping("/create")
	public String createPorduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
		if(productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto","imageFile","The Image File is Required"));
		}
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		
		//Save image in folder
		MultipartFile image =  productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime()+"-"+image.getOriginalFilename();
		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			try(InputStream inputStream=image.getInputStream()){
				Files.copy(inputStream,Paths.get(uploadDir+storageFileName),StandardCopyOption.REPLACE_EXISTING);
			}
		}catch(Exception ex) {
			System.out.println("Exception: "+ ex.getMessage());
		}
		
		Products product = new Products();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(createdAt);
		product.setImageFileName(storageFileName);
		
		
		repo.save(product);
		
		
		return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id){
		
		try {

			Products product =repo.findById(id).get();
			model.addAttribute("product",product);
			
			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());
			
			model.addAttribute("productDto", productDto);
		}catch(Exception ex){
			System.out.println("Exception : " + ex.getMessage());
			return "redirect:/products";
		}
		return "products/EditPorduct";
	}
	
	@PostMapping("/edit")
	public String updateProduct(Model model, @RequestParam int id,@Valid @ModelAttribute ProductDto productDto,BindingResult result) {
		
		try {
			Products product = repo.findById(id).get();
			model.addAttribute("product", product);
			if(result.hasErrors()) {
				return "products/EditPorduct";
			}
			if(!productDto.getImageFile().isEmpty()) {
				//Delete old image
				String uploadDir = "public/images/";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
				try {
					Files.delete(oldImagePath);
				}catch(Exception ex) {
					System.out.println("Exception : " + ex.getMessage());
				}
				//Save new Image file
				MultipartFile image = productDto.getImageFile();
				Date createdAt = new Date();
				String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
				try(InputStream inputStream = image.getInputStream()) {
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageFileName(storageFileName);
			}
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			repo.save(product);
		}catch(Exception ex) {
			System.out.println("Exception : " +ex.getMessage());
		}
		
		return "redirect:/products";
	}
	@GetMapping("/delete")
	public String deleteProduct( @RequestParam int id) {
		try {
			Products product = repo.findById(id).get();
			//Delete Image Path
			Path imagePath = Paths.get("public/images/"+product.getImageFileName());
			try {
				Files.delete(imagePath);
			}catch(Exception ex) {
				System.out.println("Exception "+ex.getMessage());
			}
			//delete the product
			repo.delete(product);
		}catch(Exception ex) {
			System.out.println("Exception : " + ex.getMessage());
		}
		return "redirect:/products";
	}
}
