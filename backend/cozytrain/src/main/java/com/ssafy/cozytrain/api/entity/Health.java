package com.ssafy.cozytrain.api.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Health {
    @Id
    @Column(name = "health_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long healthId;

    @OneToMany(mappedBy = "health", cascade = CascadeType.ALL)
    private List<SleepStage> sleepStages;

    @OneToOne
    @JoinColumn(name = "report_id", referencedColumnName = "report_id")
    private Report report;

    private int stressLevel;
    @NotNull
    private int sleepDuration;
    private int steps;
    private int sleepScore;

    @Builder
    public Health(int stressLevel, int sleepDuration, int steps){
        this.stressLevel = stressLevel;
        this.sleepDuration = sleepDuration;
        this.steps = steps;
    }
}
