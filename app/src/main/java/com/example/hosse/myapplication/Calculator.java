package com.example.hosse.myapplication;

public class Calculator {

    public int eloCalculator(double userElo,double opponentElo, int isOne){
        double expecteduser = ((1) / (1 + Math.pow(10,((opponentElo - userElo) / 400.0))));

        int updatedScore = (int) (userElo + 32*(isOne-expecteduser));
        return updatedScore;



    }
}
