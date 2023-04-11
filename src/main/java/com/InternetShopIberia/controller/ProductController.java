package com.InternetShopIberia.controller;

import com.InternetShopIberia.dto.Filter;
import com.InternetShopIberia.dto.FilterList;
import com.InternetShopIberia.dto.FilterValue;
import com.InternetShopIberia.dto.ProductList;
import com.InternetShopIberia.model.Category;
import com.InternetShopIberia.model.Product;
import com.InternetShopIberia.service.CategoryService;
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

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String showProductCategoryPage(@RequestParam(value = "categoryId", required = false) String categoryId, @RequestParam Map<String,String> allRequestParams, @ModelAttribute("filters") FilterList filterList, @ModelAttribute("products") ProductList productList, HttpSession session, Model model){
        allRequestParams.remove("categoryId");
        if(categoryId != null && !categoryId.isEmpty() && allRequestParams.isEmpty()) {
            ProductList products = new ProductList();
            products.setProducts(new ArrayList<>());
            products.getProducts().addAll(productService.getAllProductsInCategoryById(Long.parseLong(categoryId)));
            model.addAttribute("products", products);
            model.addAttribute("filters", null);
        }else {
            allRequestParams.remove("categoryId");
            var filteredProducts = getProducts(session, categoryId, allRequestParams);

            TreeMap<String, TreeSet<String>> details = new TreeMap<>();
            for(var product: filteredProducts.getProducts()){
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
            var filters = getFilters(details, allRequestParams);

            model.addAttribute("filters", filters);
            model.addAttribute("products", filteredProducts);
        }
        return "products";
    }

    @GetMapping("/products/filter")
    public RedirectView filterProducts(@RequestParam Map<String,String> allRequestParams, HttpSession session, RedirectAttributes redirectAttributes){
        var filteredProducts = getProducts(session, allRequestParams.get("categoryId"), allRequestParams);
        redirectAttributes.addFlashAttribute("products", filteredProducts);

        TreeMap<String, TreeSet<String>> details = new TreeMap<>();
        for(var product: filteredProducts.getProducts()){
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
        var filters = getFilters(details, allRequestParams);
        redirectAttributes.addFlashAttribute("filters", filters);


        RedirectView redirView;
        StringBuilder filterStr = new StringBuilder();
        allRequestParams.forEach((name, value) -> {
            if(!name.equals("categoryId") && !name.equals("searchRequest"))
                filterStr.append(name).append("=").append(value).append("&");
        });
        if(filterStr.length() != 0)
            filterStr.deleteCharAt(filterStr.length()-1);
        if(allRequestParams.get("categoryId") != null && !allRequestParams.get("categoryId").isEmpty()) {
            redirView = new RedirectView("/products?categoryId="+allRequestParams.get("categoryId") + "&" + filterStr.toString(), true);
        }else
            redirView = new RedirectView("/products?" + filterStr.toString(), true);

        return redirView;
    }

    private FilterList getFilters(TreeMap<String, TreeSet<String>> details, Map<String,String> allRequestParams){
        FilterList filters = new FilterList();
        filters.setFilters(new ArrayList<>());
        details.forEach((s, strings) -> {
            Filter filter = new Filter();
            filter.setName(s);
            List<FilterValue> filterValues = new ArrayList<>();
            strings.forEach(value -> {
                if(allRequestParams.containsValue(value)) {
                    filterValues.add(new FilterValue(value, true));
                }
                else
                    filterValues.add(new FilterValue(value, false));
            });
            filter.setValues(filterValues);
            filters.getFilters().add(filter);
        });
        return filters;
    }

    private ProductList getProducts(HttpSession session, String categoryId, Map<String,String> allRequestParams){
        List<Product> productList = null;
        if(session.getAttribute("searchRequest") != null) {
            String searchRequest = (String) session.getAttribute("searchRequest");
            productList = productService.getAllProductsNameLike(searchRequest);
        }else {
            productList = productService.getAllProductsInCategoryById(Long.parseLong(categoryId));
        }

        ProductList filteredProducts = new ProductList();
        filteredProducts.setProducts(new ArrayList<>());
        for(var product: productList) {
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
        return filteredProducts;
    }
}
