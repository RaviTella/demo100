package com.example.demo100;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Container(containerName = "Recommendation")
public class Recommendation {
	private String id;
	private String isbn;
	private String title;
	@PartitionKey
	private String author;
	private String description;
	private String imageURL;

	public Recommendation() {}

	public Recommendation(String id, String isbn, String title, String author, String description,
						  String imageURL) {
		super();
		this.id = id;		
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.description = description;
		this.imageURL = imageURL;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	@Override
	public String toString() {
		return "Recommendation{" +
				"id='" + id + '\'' +
				", isbn='" + isbn + '\'' +
				", title='" + title + '\'' +
				", author='" + author + '\'' +
				", description='" + description + '\'' +
				", imageURL='" + imageURL + '\'' +
				'}';
	}
}
