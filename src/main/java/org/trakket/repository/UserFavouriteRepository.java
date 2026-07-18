package org.trakket.repository;

import org.trakket.enums.FavouriteType;
import org.trakket.model.User;
import org.trakket.model.UserFavourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavouriteRepository extends JpaRepository<UserFavourite, Long> {

    List<UserFavourite> findByUser(User user);

    Optional<UserFavourite> findByUserAndFavouriteTypeAndFavouriteValue(
            User user, FavouriteType favouriteType, String favouriteValue
    );

    void deleteByUserAndFavouriteTypeAndFavouriteValue(
            User user, FavouriteType favouriteType, String favouriteValue
    );

    boolean existsByUserAndFavouriteTypeAndFavouriteValue(
            User user, FavouriteType favouriteType, String favouriteValue
    );
}
