package org.example.component;

import org.example.entity.ChildEntity;
import org.example.entity.ParentEntity;
import org.example.repository.ChildRepository;
import org.example.repository.ParentRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * This class is reponsible to populate the in-memory database
 * so that we already can have some data to retrieve and test on.
 * Note that, when restarting the application, all data is reset;
 * so no changes are persisted anywhere.
 */
@Component
public class DataInitializer implements InitializingBean {
  private final ParentRepository parentRepository;
  private final ChildRepository childRepository;
  public DataInitializer(ParentRepository parentRepository, ChildRepository childRepository) {
    this.parentRepository = parentRepository;
      this.childRepository = childRepository;
  }

  /**
   * Populate the database after all the (Spring) bean properties have been set
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    // persist new items
    ParentEntity parentEntity = new ParentEntity();
    parentEntity.setName("test");
    parentRepository.save(parentEntity);


    ParentEntity parentEntity1 = new ParentEntity();
    parentEntity1.setName("test1");
    parentRepository.save(parentEntity1);


    ChildEntity parentEntity2 = new ChildEntity();
    parentEntity2.setName("child");
    parentEntity2.setParentEntity(parentEntity);
    childRepository.save(parentEntity2);


    ChildEntity parentEntity3 = new ChildEntity();
    parentEntity3.setName("child1");
    parentEntity3.setParentEntity(parentEntity);
    childRepository.save(parentEntity3);

    parentRepository.findAll();
//    itemRepository.save(phoneChargerItem);
//    itemRepository.save(backpackItem);
//    itemRepository.save(shoeItem);
//    itemRepository.save(tShirtItem);

  }
}
