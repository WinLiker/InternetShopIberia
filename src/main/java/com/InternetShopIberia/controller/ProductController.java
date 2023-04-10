package com.InternetShopIberia.controller;

import com.InternetShopIberia.dto.Filter;
import com.InternetShopIberia.dto.FilterList;
import com.InternetShopIberia.dto.ProductList;
import com.InternetShopIberia.model.Product;
import com.InternetShopIberia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String showProductCategoryPage(@RequestParam(value = "categoryId", required = false) String categoryId, @ModelAttribute("filters") FilterList filterList, @ModelAttribute("products") ProductList productList, Model model){
        if(categoryId != null) {
            ProductList products = new ProductList();
            products.setProducts(new ArrayList<>());
            products.getProducts().addAll(productService.getAllProductsInCategoryById(Long.parseLong(categoryId)));
            model.addAttribute("products", products);
            model.addAttribute("filters", null);
        }else {
            model.addAttribute("filters", filterList);
            model.addAttribute("products", productList);
        }
        return "products";
    }

    @GetMapping("/products/filter")
    public RedirectView filterProducts(@RequestParam Map<String,String> allRequestParams, @ModelAttribute("products") ProductList productList, RedirectAttributes redirectAttributes){
        TreeMap<String, TreeSet<String>> details = new TreeMap<>();
        for(var product: productList.getProducts()){
            for(var detail: product.getDetails()){
                if(details.get(detail.getName()) == null){
                    var value = new TreeSet<String>();
                    value.add(detail.getValue());
                    details.put(detail.getName(), value);
                }else {
                    details.get(detail.getName()).add(detail.getValue());
                }
            }
        }
        FilterList filters = new FilterList();
        filters.setFilters(new ArrayList<>());
        details.forEach((s, strings) -> {
            Filter filter = new Filter();
            filter.setName(s);
            filter.setValues(strings.stream().toList());
            filters.getFilters().add(filter);
        });
        redirectAttributes.addFlashAttribute("filters", filters);

        allRequestParams.remove("products");
        System.out.println(allRequestParams);

        ProductList filteredProducts = new ProductList();
        filteredProducts.setProducts(new ArrayList<>());
        for(var product: productList.getProducts()) {
            AtomicBoolean fit = new AtomicBoolean(true);
            for(var detail: product.getDetails()) {
                allRequestParams.forEach((name, value) -> {
                    if(detail.getName().equals(name)){
                        if(!detail.getValue().equals(value)) {
                            fit.set(false);
                            return;
                        }
                    }
                });
                if(!fit.get()){
                    break;
                }
            }
            if(fit.get()){
                filteredProducts.getProducts().add(product);
            }
        }


        redirectAttributes.addFlashAttribute("products", filteredProducts);

        final RedirectView redirectView = new RedirectView("/products", true);
        return redirectView;
    }
}
