<?php

namespace App\DataFixtures;

use App\Entity\User;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class UserFixtures extends Fixture
{
    public const USER_REF_PREFIX = 'user_';

    public function __construct(private UserPasswordHasherInterface $passwordHasher) {}

    public function load(ObjectManager $manager): void
    {
        $usersData = [
            ['id' => 1, 'email' => 'user1@example.com', 'password' => 'password1', 'roles' => ['ROLE_USER']],
            ['id' => 2, 'email' => 'user2@example.com', 'password' => 'password2', 'roles' => ['ROLE_USER']],
            ['id' => 3, 'email' => 'user3@example.com', 'password' => 'password3', 'roles' => ['ROLE_USER']],
        ];

        foreach ($usersData as $data) {
            $user = new User();
            $user->setEmail($data['email']);
            $user->setRoles($data['roles']);
            $user->setPassword($this->passwordHasher->hashPassword($user, $data['password']));

            $manager->persist($user);
            $this->addReference(self::USER_REF_PREFIX.$data['id'], $user);
        }

        $manager->flush();
    }
}
