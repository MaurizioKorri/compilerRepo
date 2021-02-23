(do
    (read x)
    (read y)
    (read z)
    (cond (> x y)
        (cond (> x z) (print x) (else (print z)))
        (else
            (cond (> y z) (print y) (else (print z)))
        )
    )
)

e
