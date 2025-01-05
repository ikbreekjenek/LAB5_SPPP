package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Scanner;

@SpringBootApplication
public class SpringJpaConsoleApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringJpaConsoleApp.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(CommandHandler commandHandler) {
        return args -> commandHandler.start();
    }
}

@Entity
class EntityModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public EntityModel() {}

    public EntityModel(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EntityModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

interface EntityRepository extends JpaRepository<EntityModel, Long> {}

@Service
class EntityService {
    private final EntityRepository entityRepository;

    public EntityService(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    public Iterable<EntityModel> findAll() {
        return entityRepository.findAll();
    }

    public EntityModel findById(Long id) {
        return entityRepository.findById(id).orElse(null);
    }

    public EntityModel addEntity(String name) {
        return entityRepository.save(new EntityModel(name));
    }

    public EntityModel updateEntity(Long id, String name) {
        return entityRepository.findById(id)
                .map(entity -> {
                    entity.setName(name);
                    return entityRepository.save(entity);
                })
                .orElse(null);
    }

    public boolean deleteEntity(Long id) {
        if (entityRepository.existsById(id)) {
            entityRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

@Component
class CommandHandler {
    private final EntityService entityService;

    public CommandHandler(EntityService entityService) {
        this.entityService = entityService;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter command (find-all, find <id>, add <name>, update <id> <name>, delete <id>, exit):");
            String command = scanner.nextLine();
            if (command.equalsIgnoreCase("exit")) {
                break;
            } else if (command.equalsIgnoreCase("find-all")) {
                entityService.findAll().forEach(System.out::println);
            } else if (command.startsWith("find")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    Long id = Long.parseLong(parts[1]);
                    EntityModel entity = entityService.findById(id);
                    System.out.println(entity != null ? entity : "Entity not found.");
                } else {
                    System.out.println("Invalid command. Usage: find <id>");
                }
            } else if (command.startsWith("add")) {
                String[] parts = command.split(" ", 2);
                if (parts.length == 2) {
                    EntityModel entity = entityService.addEntity(parts[1]);
                    System.out.println("Added: " + entity);
                } else {
                    System.out.println("Invalid command. Usage: add <name>");
                }
            } else if (command.startsWith("update")) {
                String[] parts = command.split(" ", 3);
                if (parts.length == 3) {
                    Long id = Long.parseLong(parts[1]);
                    EntityModel entity = entityService.updateEntity(id, parts[2]);
                    System.out.println(entity != null ? "Updated: " + entity : "Entity not found.");
                } else {
                    System.out.println("Invalid command. Usage: update <id> <name>");
                }
            } else if (command.startsWith("delete")) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    Long id = Long.parseLong(parts[1]);
                    boolean deleted = entityService.deleteEntity(id);
                    System.out.println(deleted ? "Entity deleted." : "Entity not found.");
                } else {
                    System.out.println("Invalid command. Usage: delete <id>");
                }
            } else {
                System.out.println("Unknown command.");
            }
        }
    }
}
