CREATE TABLE department
(
  department_id   INT           AUTO_INCREMENT PRIMARY KEY,
  department_name VARCHAR(100)  NOT NULL
);


CREATE TABLE employee (
  employee_id         INT           AUTO_INCREMENT PRIMARY KEY,
  employee_firstname  VARCHAR (100) NOT NULL,
  employee_lastname   VARCHAR (100) NOT NULL,
  department_id       INT           NOT NULL,

  FOREIGN KEY (department_id) REFERENCES public.department (department_id)
);