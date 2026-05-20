CREATE TABLE Students (
    sid INTEGER PRIMARY KEY,
    sname VARCHAR2(50)
);

CREATE TABLE Courses (
    cid INTEGER PRIMARY KEY,
    cname VARCHAR2(100),
    credits INTEGER
);

CREATE TABLE Enrolled(
    sid INTEGER,
    cid INTEGER,
    PRIMARY KEY (sid, cid),
    FOREIGN KEY (sid) REFERENCES Students(sid),
    FOREIGN KEY (cid) REFERENCES Courses(cid)
);

INSERT INTO Courses VALUES (101, "Databse Systems", 3);
INSERT INTO Courses VALUES (102, "Algorithms", 4);
INSERT INTO Courses VALUES (103, "Operating Systems", 4);

COMMIT;