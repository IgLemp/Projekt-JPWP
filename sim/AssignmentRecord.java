
package com.transport.sim;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentRecord {
    private final int id;
    private final String title;
    private final int turnCompleted;
    private final String vehicleName;
    private final String driverName;
    private final double baseReward;
    private final double skillModifier;
    private final double netRevenue;
    private final String status;
    private final int driverSkill; // For filtering
}
