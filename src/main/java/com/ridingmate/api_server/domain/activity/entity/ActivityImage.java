package com.ridingmate.api_server.domain.activity.entity;

import com.ridingmate.api_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activity_images")
public class ActivityImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private ActivityImage(Activity activity, String imagePath, Integer displayOrder) {
        this.activity = activity;
        this.imagePath = imagePath;
        this.displayOrder = displayOrder;
    }
    /**
     * 표시 순서 업데이트
     */
    public void updateDisplayOrder(Integer newDisplayOrder) {
        this.displayOrder = newDisplayOrder;
    }

}
