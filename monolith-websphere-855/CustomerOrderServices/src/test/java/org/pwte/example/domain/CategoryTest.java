package org.pwte.example.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.util.ArrayList;
import java.util.Collection;

@DisplayName("Category Domain Model Tests")
class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
    }

    @Test
    @DisplayName("Should create Category with default constructor")
    void testDefaultConstructor() {
        Category newCategory = new Category();
        assertThat(newCategory).isNotNull();
    }

    @Test
    @DisplayName("Should set and get categoryID")
    void testCategoryID() {
        category.setCategoryID(100);
        assertThat(category.getCategoryID()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should set and get name")
    void testName() {
        category.setName("Electronics");
        assertThat(category.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should set and get parent category")
    void testParent() {
        Category parentCategory = new Category();
        parentCategory.setCategoryID(1);
        parentCategory.setName("Parent");

        category.setParent(parentCategory);
        assertThat(category.getParent()).isEqualTo(parentCategory);
        assertThat(category.getParent().getName()).isEqualTo("Parent");
    }

    @Test
    @DisplayName("Should handle null parent category")
    void testNullParent() {
        category.setParent(null);
        assertThat(category.getParent()).isNull();
    }

    @Test
    @DisplayName("Should set and get subcategories")
    void testSubCategories() {
        Collection<Category> subCategories = new ArrayList<>();
        
        Category sub1 = new Category();
        sub1.setCategoryID(101);
        sub1.setName("Laptops");
        
        Category sub2 = new Category();
        sub2.setCategoryID(102);
        sub2.setName("Phones");
        
        subCategories.add(sub1);
        subCategories.add(sub2);

        category.setSubCategories(subCategories);
        assertThat(category.getSubCategories()).hasSize(2);
        assertThat(category.getSubCategories()).contains(sub1, sub2);
    }

    @Test
    @DisplayName("Should handle empty subcategories collection")
    void testEmptySubCategories() {
        Collection<Category> emptySubCategories = new ArrayList<>();
        category.setSubCategories(emptySubCategories);
        assertThat(category.getSubCategories()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get products")
    void testProducts() {
        Collection<Product> products = new ArrayList<>();
        
        Product product1 = new Product();
        product1.setProductId(1);
        product1.setName("Product 1");
        
        Product product2 = new Product();
        product2.setProductId(2);
        product2.setName("Product 2");
        
        products.add(product1);
        products.add(product2);

        category.setProducts(products);
        assertThat(category.getProducts()).hasSize(2);
        assertThat(category.getProducts()).contains(product1, product2);
    }

    @Test
    @DisplayName("Should be Serializable")
    void testSerializable() {
        assertThat(category).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Should create category hierarchy")
    void testCategoryHierarchy() {
        // Root category
        Category root = new Category();
        root.setCategoryID(1);
        root.setName("Root");

        // Child category
        Category child = new Category();
        child.setCategoryID(2);
        child.setName("Child");
        child.setParent(root);

        // Grandchild category
        Category grandchild = new Category();
        grandchild.setCategoryID(3);
        grandchild.setName("Grandchild");
        grandchild.setParent(child);

        assertThat(grandchild.getParent()).isEqualTo(child);
        assertThat(grandchild.getParent().getParent()).isEqualTo(root);
        assertThat(grandchild.getParent().getParent().getName()).isEqualTo("Root");
    }
}
