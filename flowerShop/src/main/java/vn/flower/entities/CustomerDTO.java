package vn.flower.entities;

import java.time.LocalDateTime;

public class CustomerDTO {
    private Integer id;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private long orderCount;

    public CustomerDTO(Integer id, String fullName, String email, LocalDateTime createdAt, long orderCount) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.orderCount = orderCount;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public long getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(long orderCount) {
		this.orderCount = orderCount;
	}

    
}
