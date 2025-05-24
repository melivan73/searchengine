package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "lemma")
@Getter
public final class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    private Site siteId;
    @Column(name="lemma")
    String lemma;
    @Column(name="frequency")
    int frequency;
}
