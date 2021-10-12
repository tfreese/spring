CREATE TABLE IF NOT EXISTS department
(
  department_id   INT           AUTO_INCREMENT PRIMARY KEY,
  department_name VARCHAR(100)  NOT NULL
);


CREATE TABLE IF NOT EXISTS employee (
  employee_id         INT           AUTO_INCREMENT PRIMARY KEY,
  employee_lastname   VARCHAR (100) NOT NULL,  
  employee_firstname  VARCHAR (100) NOT NULL,
  department_id       INT           NOT NULL,

  FOREIGN KEY (department_id) REFERENCES department (department_id)
);