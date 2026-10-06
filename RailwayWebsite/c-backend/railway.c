#include<stdio.h>
#include<string.h>
#include<time.h>
struct passenger{
    char name[100];
    int age;
    char gender[100];
    int tnumber;
    char tname[100];
    char source[100];
    char destination[100];
    char date[100];
    char class[100];
    char preference[100];
    int pnr;
    int seatNo;
};
void booking();
void reserved();
void search();
void schedule();

int main(){
    int a;
    printf("=========================================================\n");
    printf("                        INDIAN RAILWAYS                             \n");
    printf("=========================================================\n");

    printf("1. Book Ticket \n");
    printf("2. View Reserved Tickets \n");
    printf("3. Search Reservation \n");
    printf("4. Train Schedule \n");
    printf("Enter your choice:");
    scanf("%d",&a);
    if(a>=5  |  a<=0){
        printf("choice must be in between 1-4");
    }
    if(a==1){
        srand(time(NULL));
        booking();

        
    }
    else if(a==2){
        reserved();
    }
    else if(a==3){
        search();
    }
    else if(a==4){
        srand(time(NULL));
        schedule();
    }
    return 0;
}
void booking(){
    char pname[50];
    char pgender[50];
    struct passenger p1;

    printf("enter name:");
    scanf(" %49[^\n]",&pname);
    strcpy(p1.name,pname);

    printf("enter age:");
    scanf("%d",&p1.age);

    printf("enter gender:");
    scanf(" %49[^\n]",&pgender);
    strcpy(p1.gender,pgender);

    printf("enter train number:");
    scanf("%d",&p1.tnumber);

    char trname[50],psource[50],pdestination[50],tdate[50];

    printf("enter Train name:");
    scanf(" %49[^\n]",&trname);
    strcpy(p1.tname,trname);

    printf("enter source:");
    scanf(" %49[^\n]",&psource);
    strcpy(p1.source,psource);


    printf("enter destination:");
    scanf(" %49[^\n]",&pdestination);
    strcpy(p1.destination,pdestination);

    printf("enter date:");
    scanf(" %49[^\n]",&tdate);
    strcpy(p1.date,tdate);

    char pclass[50],ppreference[50];

    printf("enter class:");
    scanf(" %49[^\n]",&pclass);
    strcpy(p1.class,pclass);


    printf("enter preference:");
    scanf(" %49[^\n]",&ppreference);
    strcpy(p1.preference,ppreference);

    printf("------------------------------------------------\n");

    printf("Passenger name            :%s\n",p1.name);
    printf("Passenger age             :%d\n",p1.age);
    printf("Passenger gender          :%s\n",p1.gender);
    printf("Train number              :%d\n",p1.tnumber);
    printf("Train name                :%s\n",p1.tname);
    printf("Source                    :%s\n",p1.source);
    printf("Destination               :%s\n",p1.destination);
    printf("traveling date            :%s\n",p1.date);
    printf("class                     :%s\n",p1.class);
    printf("seat preference           :%s\n",p1.preference);
    
    
    printf("\n");
    printf("\n");
    printf("\n");
    printf("Booking confirmed!!\n");
    printf("----------------------------------------------------------------\n"); 

    int pnr,seatNo;
    pnr = rand() % 900000 + 800000;
    seatNo = rand() % 72 + 1; 
    p1.pnr=pnr;
    p1.seatNo=seatNo;
    printf("PNR Number  : %d\n", pnr);
    printf("coach       : B2\n");
    printf("Seat Number : %d\n", seatNo);
    printf("Status      : Confirmed\n");

    FILE *fptr;
    fptr=fopen("Railway.dat","ab");
    fwrite(&p1,sizeof(p1),1,fptr);
    fclose(fptr);



}

void reserved(){
    struct passenger p1;
    FILE *fptr;
    printf("----------------------------------------------------------------\n");
    fptr=fopen("Railway.dat","rb");
    int pnr;
    p1.pnr=pnr;
    


    while(fread(&p1,sizeof(struct passenger),1,fptr)==1){
        
        printf("Passenger name            :%s\n",p1.name);
        printf("Passenger PNR             :%d\n",p1.pnr);
        printf("Passenger age             :%d\n",p1.age);
        printf("Passenger gender          :%s\n",p1.gender);
        printf("Train number              :%d\n",p1.tnumber);
        printf("Train name                :%s\n",p1.tname);
        printf("Source                    :%s\n",p1.source);
        printf("Destination               :%s\n",p1.destination);
        printf("traveling date            :%s\n",p1.date);
        printf("class                     :%s\n",p1.class);
        printf("seat preference           :%s\n",p1.preference);
        printf("----------------------------------------------------------------\n");
        
    }
    fclose(fptr);
    
    
}


void search(){
    struct passenger p1;
    int c,x;
    printf("Enter PNR:");
    scanf("%d",&c);

    FILE *fptr;
    fptr=fopen("Railway.dat","rb");
    
    
    while(fread(&p1,sizeof(struct passenger),1,fptr)==1){

        
        if(p1.pnr==c){
        printf("Booking found!\n");
        printf("-----------------------------------------------------\n");
        printf("Name                 :%s\n",p1.name);
        printf("Train Number         :%d\n",p1.tnumber);
        printf("Train Name           :%s\n",p1.tname);
        printf("Date                 :%s\n",p1.date);
        printf("coach                :B2\n");    
        printf("Seat number          :%d\n",p1.seatNo);
        printf("Status               :Confirmed!\n");
        x=c;
        break;
        }

    }
    if(x!=c){
            printf("NO DATA FOUND!!!!\n");
        }
    fclose(fptr);

}

void schedule(){
    struct passenger p1;
    FILE *fptr;
    fptr=fopen("Railway.dat","rb");
    printf("----------------------------TRAIN SCHEDULE-----------------------------------------------------------\n");
    printf("Train No          Train name                            Departure               Arrival\n");
    while(fread(&p1,sizeof(struct passenger),1,fptr)==1){
        int dephour = rand() % 24;    // 0 to 23
        int depminute = rand() % 60;  // 0 to 59
        int arrhour=(dephour+4)%24;
        int arrmin=(depminute+4)%60;
        

        printf("%d             %-20s                  %02d:%02d                   %02d:%02d\n",p1.tnumber,p1.tname,dephour,depminute,arrhour,arrmin);
    }
    printf("-----------------------------------------------------------------------------------------------------\n");
    fclose(fptr);


}