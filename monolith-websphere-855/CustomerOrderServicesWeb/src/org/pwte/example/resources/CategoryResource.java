package org.pwte.example.resources;

import java.util.List;

import jakarta.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.pwte.example.domain.Category;
import org.pwte.example.exception.CategoryDoesNotExist;
import org.pwte.example.service.ProductSearchService;

@Path("/Category")
public class CategoryResource 
{
	@EJB ProductSearchService productSearch;
	
	public CategoryResource() throws NamingException
	{
		//Work around until Java EE 6
		productSearch = (ProductSearchService) 
		new InitialContext().lookup("ejblocal:org.pwte.example.service.ProductSearchService");
	}
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Category loadCategory(@PathParam(value="id") int categoryId)
	{
		try {
			return productSearch.loadCategory(categoryId);
		} catch (CategoryDoesNotExist e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Category> loadTopLevelCategories()
	{
		return productSearch.getTopLevelCategories();
	}
	
}