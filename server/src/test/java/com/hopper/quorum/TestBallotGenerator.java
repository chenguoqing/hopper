package com.hopper.quorum;


import org.junit.Assert;
import org.junit.Test;

public class TestBallotGenerator {

    @Test
    public void test() {
        int ballot = BallotGenerator.generateBallot(0, 3, 0);
        Assert.assertEquals(ballot, 3);

        ballot = BallotGenerator.generateBallot(0, 3, 3);
        Assert.assertEquals(ballot, 6);

        ballot = BallotGenerator.generateBallot(1, 3, 0);
        Assert.assertEquals(ballot, 4);

        ballot = BallotGenerator.generateBallot(1, 3, 4);
        Assert.assertEquals(ballot, 7);
    }
}
