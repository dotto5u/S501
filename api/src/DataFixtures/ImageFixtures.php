<?php

namespace App\DataFixtures;

use App\Entity\Image;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;
use Doctrine\Common\DataFixtures\DependentFixtureInterface;

class ImageFixtures extends Fixture implements DependentFixtureInterface
{
    public const IMAGE_REF_PREFIX = 'image_';

    public function load(ObjectManager $manager): void
    {
        $imagesData = [
            [
                'id' => 1,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/1',
                'categories_ref' => ['category_1', 'category_2'],
                'user_ref' => 'user_1',
            ],
            [
                'id' => 2,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/2',
                'categories_ref' => ['category_3', 'category_4'],
                'user_ref' => 'user_2',
            ],
            [
                'id' => 3,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/3',
                'categories_ref' => ['category_5', 'category_6'],
                'user_ref' => 'user_3',
            ],
            [
                'id' => 4,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/4',
                'categories_ref' => ['category_1', 'category_7'],
                'user_ref' => 'user_1',
            ],
            [
                'id' => 5,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/5',
                'categories_ref' => ['category_1', 'category_8'],
                'user_ref' => 'user_2',
            ],
            [
                'id' => 6,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/6',
                'categories_ref' => ['category_1', 'category_2', 'category_4'],
                'user_ref' => 'user_3',
            ],
            [
                'id' => 7,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/7',
                'categories_ref' => ['category_1', 'category_6'],
                'user_ref' => 'user_1',
            ],
            [
                'id' => 8,
                'imageId' => uniqid(),
                'url' => 'https://via.placeholder.com/150/8',
                'categories_ref' => ['category_3', 'category_5', 'category_4'],
                'user_ref' => 'user_2',
            ],
        ];

        foreach ($imagesData as $data) {
            $image = new Image();
            $image->setImageId($data['imageId']);
            $image->setUrl($data['url']);

            foreach ($data['categories_ref'] as $categoryRef) {
                $image->addCategory($this->getReference($categoryRef));
            }

            $image->setUser($this->getReference($data['user_ref']));

            $manager->persist($image);
            $this->addReference(self::IMAGE_REF_PREFIX . $data['id'], $image);
        }

        $manager->flush();
    }

    public function getDependencies(): array
    {
        return [
            CategoryFixtures::class,
            UserFixtures::class,
        ];
    }
}
