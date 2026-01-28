import java.util.Scanner;
public class ArrayObjectsTasks {
    public static void main(String[] args){
        task1_linearSearch();
        task2_bubbleSort();
        task3_arrayOfObjects();
        task4_findMostExpensiveBook();
        task5_displayBookEnhancedLoop();
    }
    
        public static void task1_linearSearch(){
            int arrayone[]={12,45,7,23,9};//the main array
            Scanner scanner=new Scanner(System.in);//scanner
            System.out.println("Enter a number to search: ");//ask for input
            int inone=scanner.nextInt();//take input
            boolean boolone=true;//initialize boolean
            int index=0;//initialize index holder
            for (int i=0;i<arrayone.length;i++){//for loop that checks evey item in array
                if (inone==arrayone[i]){//if input equals item in array
                    boolone=true;//turns the boolean to true
                    index=i+1;//holds index
                }
            }
            if(boolone){//if boolean is true
                System.out.println("Number is at index: "+index);//print index
            }else{//otherwise
                System.out.println("Number is not in index");//print not in array
            }
        }
            
        public static void task2_bubbleSort(){
            int arraytwo[]={29,10,24,37,13};//initialize array
            int a=0,b=0;//initialize int
        
            //print array
            System.out.print("Before sort: ");
            for (int j=0;j<arraytwo.length;j++){
                System.out.print(arraytwo[j]+" ");
            }
        
            //Sort
            for (int l=0;l<arraytwo.length;l++)//for loop length of array
                for (int k=0;k<arraytwo.length;k++){//nested for loop length of array
                    if (k+1<arraytwo.length){//if not the end number
                        if (arraytwo[k]>arraytwo[k+1]){//if current number larger than next number
                            a=arraytwo[k];//set a to current number
                            b=arraytwo[k+1];//set b to next number
                            arraytwo[k]=b;//set current number to b
                            arraytwo[k+1]=a;//set next number to a
                        }
                    }
                }
    
            //print array after sort
            System.out.print("After: ");
            for (int j=0;j<arraytwo.length;j++){
                System.out.print(arraytwo[j]+" ");
            }
        }
        
        static class Book {
            public String title;
            public double price;

            public Book(){
                title="";
                price=0.0;
            }
            
            public Book(String title,double price){
                this.title=title;
                this.price=price;
            }

            void display(){
                System.out.println("Title: "+title+" | Price: "+price);
            }
        }
        
            public static void task3_arrayOfObjects(){
        Book[] arraythree={
            new Book("Harry Potter",19.99),
            new Book("Cookbook",29.99),
            new Book("Dr. Suess",39.99)
        };
        for (Book b1:arraythree){
            b1.display();
        }
    }

    public static void task4_findMostExpensiveBook(){
        double highest=0.0;
        int index=0;

        Book[] arrayfour={
            new Book("Harry Potter",19.99),
            new Book("Cookbook",29.99),
            new Book("Dr. Suess",39.99),
            new Book("OnePiece", 24.99),
            new Book("Avatar",9.99)
        };

        for (int m=0;m<arrayfour.length;m++){
            Book b=arrayfour[m];
            if (b.price>highest){
                highest=b.price;
                index=m;
            }
        }

        System.out.println("Highest price is:");
        arrayfour[index].display();
    }

    public static void task5_displayBookEnhancedLoop(){
        Book[] arrayfive={
            new Book("Harry Potter",19.99),
            new Book("Cookbook",29.99),
            new Book("Dr. Suess",39.99),
            new Book("OnePiece", 24.99),
        };

        for (Book a:arrayfive){
            a.display();
        }
    }
}