public enum Difficulty {
    Easy("Easy", 55),
    Medium("Medium",50),
    Hard("Hard",45);

    private String difficulty;
    private int givenNumbers;

    Difficulty(String s, int n)
    {
        difficulty = s;
        givenNumbers = n;
    }

    public int getNumbers() {
        return givenNumbers;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
