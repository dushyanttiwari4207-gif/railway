#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define DATA_FILE "Railway.dat"

struct passenger {
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

void booking(void);
void reserved(void);
void search(void);
void schedule(void);

int main(void) {
    int choice;

    srand((unsigned int)time(NULL));

    printf("=========================================================\n");
    printf("                    INDIAN RAILWAYS\n");
    printf("=========================================================\n");
    printf("1. Book Ticket\n");
    printf("2. View Reserved Tickets\n");
    printf("3. Search Reservation\n");
    printf("4. Train Schedule\n");
    printf("Enter your choice:");

    if (scanf("%d", &choice) != 1) {
        printf("Invalid choice.\n");
        return 1;
    }

    if (choice < 1 || choice > 4) {
        printf("Choice must be between 1 and 4.\n");
        return 1;
    }

    if (choice == 1) {
        booking();
    } else if (choice == 2) {
        reserved();
    } else if (choice == 3) {
        search();
    } else {
        schedule();
    }

    return 0;
}

void booking(void) {
    struct passenger p1;
    FILE *file;

    memset(&p1, 0, sizeof(p1));

    printf("Enter name:");
    scanf(" %99[^\n]", p1.name);

    printf("Enter age:");
    if (scanf("%d", &p1.age) != 1) {
        printf("Invalid age.\n");
        return;
    }

    printf("Enter gender:");
    scanf(" %99[^\n]", p1.gender);

    printf("Enter train number:");
    if (scanf("%d", &p1.tnumber) != 1) {
        printf("Invalid train number.\n");
        return;
    }

    printf("Enter Train name:");
    scanf(" %99[^\n]", p1.tname);

    printf("Enter source:");
    scanf(" %99[^\n]", p1.source);

    printf("Enter destination:");
    scanf(" %99[^\n]", p1.destination);

    printf("Enter date:");
    scanf(" %99[^\n]", p1.date);

    printf("Enter class:");
    scanf(" %99[^\n]", p1.class);

    printf("Enter preference:");
    scanf(" %99[^\n]", p1.preference);

    p1.pnr = rand() % 900000 + 800000;
    p1.seatNo = rand() % 72 + 1;

    printf("------------------------------------------------\n");
    printf("Passenger name            : %s\n", p1.name);
    printf("Passenger age             : %d\n", p1.age);
    printf("Passenger gender          : %s\n", p1.gender);
    printf("Train number              : %d\n", p1.tnumber);
    printf("Train name                : %s\n", p1.tname);
    printf("Source                    : %s\n", p1.source);
    printf("Destination               : %s\n", p1.destination);
    printf("Traveling date            : %s\n", p1.date);
    printf("Class                     : %s\n", p1.class);
    printf("Seat preference           : %s\n", p1.preference);
    printf("------------------------------------------------\n");
    printf("Booking confirmed!!\n");
    printf("PNR Number                : %d\n", p1.pnr);
    printf("Coach                     : B2\n");
    printf("Seat Number               : %d\n", p1.seatNo);
    printf("Status                    : Confirmed\n");

    file = fopen(DATA_FILE, "ab");
    if (file == NULL) {
        perror("Unable to open Railway.dat");
        return;
    }

    if (fwrite(&p1, sizeof(p1), 1, file) != 1) {
        perror("Unable to save booking");
    }

    fclose(file);
}

void reserved(void) {
    struct passenger p1;
    FILE *file = fopen(DATA_FILE, "rb");
    int count = 0;

    if (file == NULL) {
        printf("No reservations found.\n");
        return;
    }

    printf("------------------------------------------------\n");

    while (fread(&p1, sizeof(p1), 1, file) == 1) {
        count++;

        printf("Passenger name            : %s\n", p1.name);
        printf("Passenger PNR             : %d\n", p1.pnr);
        printf("Passenger age             : %d\n", p1.age);
        printf("Passenger gender          : %s\n", p1.gender);
        printf("Train number              : %d\n", p1.tnumber);
        printf("Train name                : %s\n", p1.tname);
        printf("Source                    : %s\n", p1.source);
        printf("Destination               : %s\n", p1.destination);
        printf("Traveling date            : %s\n", p1.date);
        printf("Class                     : %s\n", p1.class);
        printf("Seat preference           : %s\n", p1.preference);
        printf("Coach                     : B2\n");
        printf("Seat number               : %d\n", p1.seatNo);
        printf("Status                    : Confirmed\n");
        printf("------------------------------------------------\n");
    }

    if (count == 0) {
        printf("No reservations found.\n");
    }

    fclose(file);
}

void search(void) {
    struct passenger p1;
    FILE *file;
    int pnr;
    int found = 0;

    printf("Enter PNR:");
    if (scanf("%d", &pnr) != 1) {
        printf("Invalid PNR.\n");
        return;
    }

    file = fopen(DATA_FILE, "rb");
    if (file == NULL) {
        printf("NO DATA FOUND!!!!\n");
        return;
    }

    while (fread(&p1, sizeof(p1), 1, file) == 1) {
        if (p1.pnr == pnr) {
            found = 1;

            printf("Booking found!\n");
            printf("-----------------------------------------------------\n");
            printf("Name                 : %s\n", p1.name);
            printf("Train Number         : %d\n", p1.tnumber);
            printf("Train Name           : %s\n", p1.tname);
            printf("Source               : %s\n", p1.source);
            printf("Destination          : %s\n", p1.destination);
            printf("Date                 : %s\n", p1.date);
            printf("Class                : %s\n", p1.class);
            printf("Coach                : B2\n");
            printf("Seat number          : %d\n", p1.seatNo);
            printf("Status               : Confirmed!\n");
            break;
        }
    }

    if (!found) {
        printf("NO DATA FOUND!!!!\n");
    }

    fclose(file);
}

void schedule(void) {
    struct passenger p1;
    FILE *file = fopen(DATA_FILE, "rb");
    int displayedTrainNumbers[1000];
    int displayedCount = 0;
    int found = 0;

    if (file == NULL) {
        printf("No train schedule data found.\n");
        return;
    }

    printf("---------------------------- TRAIN SCHEDULE ----------------------------\n");
    printf("%-12s %-30s %-15s %-15s\n",
           "Train No", "Train Name", "Departure", "Arrival");
    printf("------------------------------------------------------------------------\n");

    while (fread(&p1, sizeof(p1), 1, file) == 1) {
        int alreadyDisplayed = 0;
        int i;

        for (i = 0; i < displayedCount; i++) {
            if (displayedTrainNumbers[i] == p1.tnumber) {
                alreadyDisplayed = 1;
                break;
            }
        }

        if (alreadyDisplayed) {
            continue;
        }

        if (displayedCount < 1000) {
            displayedTrainNumbers[displayedCount++] = p1.tnumber;
        }

        {
            int depHour = abs(p1.tnumber) % 24;
            int depMinute = abs(p1.tnumber) % 60;
            int totalMinutes = depHour * 60 + depMinute + 240;
            int arrHour = (totalMinutes / 60) % 24;
            int arrMinute = totalMinutes % 60;

            printf("%-12d %-30s %02d:%02d          %02d:%02d\n",
                   p1.tnumber,
                   p1.tname,
                   depHour,
                   depMinute,
                   arrHour,
                   arrMinute);
        }

        found = 1;
    }

    if (!found) {
        printf("No train schedule data found.\n");
    }

    printf("------------------------------------------------------------------------\n");
    fclose(file);
}
