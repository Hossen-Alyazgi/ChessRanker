package com.example.hosse.myapplication;

public class Calculator {

    public int eloCalculator(int userElo,int opponentElo, int isOne){
        int expecteduser = ((1)/(1+10^((opponentElo-userElo)/400)));

        int updatedScore =(userElo + 32*(isOne-expecteduser));
        return updatedScore;



    }
}
