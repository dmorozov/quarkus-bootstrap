package org.acme.hibernate.orm.panache;

import java.util.List;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FruitRepository implements PanacheRepository<Fruit> {

  public Uni<List<Fruit>> findOrdered() {
    return find("ORDER BY name").list();
  }

  public Uni<Fruit> createFruit(final String name) {
    Fruit fruit = new Fruit(name);
    return persist(fruit);
  }

}
