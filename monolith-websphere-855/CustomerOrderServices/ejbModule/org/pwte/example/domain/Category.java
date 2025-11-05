package org.pwte.example.domain;

import java.io.Serializable;
import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

@Entity
@NamedQuery(name="top.level.category",query="select c from Category c where c.parent IS NULL")
public class Category implements Serializable {
	
	private static final long serialVersionUID = -2872694133550658771L;

	@Id
	@Column(name="CAT_ID")
	private int categoryID;
	
	@Column(name="CAT_NAME")
	private String name;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="PARENT_CAT")
	private Category parent;
	
	
	@OneToMany(mappedBy="parent",fetch=FetchType.EAGER)
	private Collection<Category> subCategories;
	
	@ManyToMany(mappedBy="categories",fetch=FetchType.LAZY)
	private Collection<Product> products;
	
	@JsonProperty(value="id")
	public int getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public Category getParent() {
		return parent;
	}
	public void setParent(Category parent) {
		this.parent = parent;
	}
	public Collection<Category> getSubCategories() {
		return subCategories;
	}
	public void setSubCategories(Collection<Category> subCategories) {
		this.subCategories = subCategories;
	}
	
	@JsonIgnore
	public Collection<Product> getProducts() {
		return products;
	}
	public void setProducts(Collection<Product> products) {
		this.products = products;
	}
	
	

}
