INSERT INTO DEF_ACCOUNT(CLIENT_ID, ACCOUNT_ID)
Select c.ID, a.ID From CLIENT c
JOIN ACCOUNT A on c.ID = A.OWNER
Where a.id = (Select Min(b.id)
             From ACCOUNT b
             Where b.OWNER = c.ID)
ORDER BY c.ID;