#java -Xms64m -Xmx64m -jar stage2.jar "./submit/stage2/2014038304-0.0.1-SNAPSHOT.jar" "4096" "2000"

# For all submissions
for entry in ./submit/stage2/*
do
    for m in 16 32 48 64
    do
        for blocksize in 4096 8192
        do
            for ((nblocks=1000;nblocks<=2000;nblocks+=200))
            do
                echo $entry $m $blocksize $nblocks
                java -Xms${m}m -Xmx${m}m -jar stage2.jar "${entry}" "${blocksize}" "${nblocks}"
                echo
            done
            echo
            echo
        done
        echo
        echo
    done
    echo "=============================================================================================================="
done
