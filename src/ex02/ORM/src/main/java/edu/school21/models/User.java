package edu.school21.models;


import edu.school21.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@OrmEntity(table = "users")
public class User {
    @OrmColumnId(constraints = @Constraints(primaryKey = true, allowNull = false, unique = true))
    private Integer id;
    @OrmColumn(name = "FirstName", length = 30)
    private String firstName;
    @OrmColumn(name = "LastName", length = 30)
    private String lastName;
    @OrmColumn(name = "Age")
    private Integer age;
}

