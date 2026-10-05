package com.toyland.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

	@Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.defaultAddress DESC, a.id DESC")
	List<Address> findAllOfUser(@Param("userId") Long userId);

	@Query("SELECT a FROM Address a WHERE a.id = :id AND a.user.id = :userId")
	Optional<Address> findOwn(@Param("id") Long id, @Param("userId") Long userId);

	@Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
	long countOfUser(@Param("userId") Long userId);

	@Modifying
	@Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId")
	void clearDefault(@Param("userId") Long userId);

	@Modifying
	@Query("DELETE FROM Address a WHERE a.user.id = :userId")
	void deleteAllOfUser(@Param("userId") Long userId);
}
