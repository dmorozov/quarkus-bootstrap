package org.acme.hibernate.orm.panache;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Cacheable
@Table(name = "FRUITS")
public class Fruit {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FRUITS_SEQ")
  @SequenceGenerator(name = "FRUITS_SEQ", sequenceName = "FRUITS_SEQ")
  public Long id;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + id + ">";
  }

  @Column(length = 40, unique = true)
  public String name;

  public Fruit() {
  }

  public Fruit(String name) {
    this.name = name;
  }

}
