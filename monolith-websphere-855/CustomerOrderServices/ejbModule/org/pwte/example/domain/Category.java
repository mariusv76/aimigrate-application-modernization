package org.pwte.example.domain;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;

@Entity
@NamedQuery(name="top.level.category",query="select c from Category c where c.parent IS NULL")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "categoryID")
public class Category implements Serializable {
	
	private static final long serialVersionUID = -2872694133550658771L;

	@Id
	@Column(name="CAT_ID")
	private int categoryID;
	
	@Column(name="CAT_NAME")
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PARENT_CAT")
	private Category parent;
	
	
	@OneToMany(mappedBy="parent",fetch=FetchType.LAZY)
	private Collection<Category> subCategories;
	
	@ManyToMany(mappedBy="categories",fetch=FetchType.LAZY)
	private Collection<Product> products;
	
	@JsonProperty(value="id")
	@JsonbProperty(value="id")
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
	
	@JsonbTransient
	public Category getParent() {
		return parent;
	}
	public void setParent(Category parent) {
		this.parent = parent;
	}
	
	@JsonbTransient
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
